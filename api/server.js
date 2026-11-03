require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const { Pool } = require('pg');

const app = express();
const port = Number(process.env.PORT || 4000);
const jwtSecret = process.env.JWT_SECRET || 'development-only-secret-change-me';

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.DATABASE_URL && !process.env.DATABASE_URL.includes('localhost')
    ? { rejectUnauthorized: false }
    : false
});

app.use(cors({ origin: process.env.CORS_ORIGIN || '*' }));
app.use(express.json({ limit: '2mb' }));

const { isValidEmail, isValidPassword } = require('./validation');

function createToken(user) {
  return jwt.sign(
    { sub: user.id, email: user.email, name: user.name },
    jwtSecret,
    { expiresIn: '7d' }
  );
}

function authenticate(req, res, next) {
  const header = req.headers.authorization || '';
  if (!header.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: 'Authentication required.' });
  }

  const token = header.substring(7);
  try {
    req.user = jwt.verify(token, jwtSecret);
    next();
  } catch (error) {
    return res.status(401).json({ success: false, message: 'Invalid or expired token.' });
  }
}

async function initialiseDatabase() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS workouts (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      distance_meters DOUBLE PRECISION NOT NULL,
      duration_seconds BIGINT NOT NULL,
      average_pace_seconds_per_km DOUBLE PRECISION NOT NULL,
      start_time TIMESTAMPTZ NOT NULL,
      total_elevation DOUBLE PRECISION NOT NULL DEFAULT 0,
      notes TEXT NOT NULL DEFAULT '',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS route_points (
      id BIGSERIAL PRIMARY KEY,
      workout_id TEXT NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
      latitude DOUBLE PRECISION NOT NULL,
      longitude DOUBLE PRECISION NOT NULL,
      altitude DOUBLE PRECISION NOT NULL,
      point_timestamp BIGINT NOT NULL
    );

    CREATE INDEX IF NOT EXISTS idx_workouts_user_id ON workouts(user_id);
    CREATE INDEX IF NOT EXISTS idx_route_points_workout_id ON route_points(workout_id);
  `);
}

app.get('/health', async (req, res) => {
  try {
    await pool.query('SELECT 1');
    res.json({ status: 'ok', service: 'SPRINTA API' });
  } catch (error) {
    res.status(503).json({ status: 'error', message: 'Database unavailable.' });
  }
});

app.post('/api/v1/auth/register', async (req, res) => {
  const { name, email, password } = req.body || {};

  if (!name || !email || !password) {
    return res.status(400).json({ success: false, message: 'Name, email and password are required.' });
  }

  if (!isValidEmail(email)) {
    return res.status(422).json({ success: false, message: 'Invalid email address.' });
  }

  if (!isValidPassword(password)) {
    return res.status(422).json({
      success: false,
      message: 'Password must be at least 8 characters and include an uppercase letter, a number and a special character.'
    });
  }

  try {
    const existing = await pool.query('SELECT id FROM users WHERE LOWER(email) = LOWER($1)', [email.trim()]);
    if (existing.rowCount > 0) {
      return res.status(409).json({ success: false, message: 'An account with this email already exists.' });
    }

    const passwordHash = await bcrypt.hash(password, 12);
    const user = {
      id: crypto.randomUUID(),
      name: name.trim(),
      email: email.trim().toLowerCase()
    };

    await pool.query(
      'INSERT INTO users (id, name, email, password_hash) VALUES ($1, $2, $3, $4)',
      [user.id, user.name, user.email, passwordHash]
    );

    return res.status(201).json({
      success: true,
      token: createToken(user),
      user
    });
  } catch (error) {
    console.error('Registration error:', error);
    return res.status(500).json({ success: false, message: 'Internal server error.' });
  }
});

app.post('/api/v1/auth/login', async (req, res) => {
  const { email, password } = req.body || {};

  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password are required.' });
  }

  try {
    const result = await pool.query(
      'SELECT id, name, email, password_hash FROM users WHERE LOWER(email) = LOWER($1)',
      [email.trim()]
    );

    if (result.rowCount === 0) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    const dbUser = result.rows[0];
    const valid = await bcrypt.compare(password, dbUser.password_hash);
    if (!valid) {
      return res.status(401).json({ success: false, message: 'Invalid email or password.' });
    }

    const user = { id: dbUser.id, name: dbUser.name, email: dbUser.email };
    return res.json({ success: true, token: createToken(user), user });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ success: false, message: 'Internal server error.' });
  }
});

app.post('/api/v1/runs/sync', authenticate, async (req, res) => {
  const workout = req.body || {};
  const {
    workoutId,
    distanceMeters,
    durationSeconds,
    averagePaceSecondsPerKm,
    startTimeIso,
    totalElevation = 0,
    notes = '',
    routePoints = []
  } = workout;

  if (!workoutId || !Number.isFinite(distanceMeters) || !Number.isFinite(durationSeconds) || !startTimeIso) {
    return res.status(422).json({ success: false, message: 'Invalid workout payload.' });
  }

  if (workout.userId && workout.userId !== req.user.sub) {
    return res.status(403).json({ success: false, message: 'Workout user does not match token.' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    await client.query(
      `INSERT INTO workouts
       (id, user_id, distance_meters, duration_seconds, average_pace_seconds_per_km, start_time, total_elevation, notes)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8)
       ON CONFLICT (id) DO UPDATE SET
         distance_meters = EXCLUDED.distance_meters,
         duration_seconds = EXCLUDED.duration_seconds,
         average_pace_seconds_per_km = EXCLUDED.average_pace_seconds_per_km,
         start_time = EXCLUDED.start_time,
         total_elevation = EXCLUDED.total_elevation,
         notes = EXCLUDED.notes`,
      [
        workoutId,
        req.user.sub,
        distanceMeters,
        durationSeconds,
        Number.isFinite(averagePaceSecondsPerKm) ? averagePaceSecondsPerKm : 0,
        new Date(startTimeIso),
        totalElevation,
        notes
      ]
    );

    await client.query('DELETE FROM route_points WHERE workout_id = $1', [workoutId]);

    for (const point of routePoints) {
      if (!Number.isFinite(point.latitude) || !Number.isFinite(point.longitude) || !Number.isFinite(point.timestamp)) {
        continue;
      }
      await client.query(
        `INSERT INTO route_points (workout_id, latitude, longitude, altitude, point_timestamp)
         VALUES ($1,$2,$3,$4,$5)`,
        [workoutId, point.latitude, point.longitude, Number(point.altitude || 0), point.timestamp]
      );
    }

    await client.query('COMMIT');
    return res.status(201).json({
      status: 'created',
      message: 'Workout synchronised successfully.',
      serverTimestamp: Date.now()
    });
  } catch (error) {
    await client.query('ROLLBACK');
    console.error('Workout sync error:', error);
    return res.status(500).json({ success: false, message: 'Unable to save workout.' });
  } finally {
    client.release();
  }
});

app.get('/api/v1/runs/user/:userId', authenticate, async (req, res) => {
  if (req.params.userId !== req.user.sub) {
    return res.status(403).json({ success: false, message: 'Access denied.' });
  }

  try {
    const workoutsResult = await pool.query(
      `SELECT id, user_id, distance_meters, duration_seconds,
              average_pace_seconds_per_km, start_time, total_elevation, notes
       FROM workouts
       WHERE user_id = $1
       ORDER BY start_time DESC`,
      [req.params.userId]
    );

    const workouts = [];
    for (const row of workoutsResult.rows) {
      const pointsResult = await pool.query(
        `SELECT latitude, longitude, altitude, point_timestamp
         FROM route_points WHERE workout_id = $1 ORDER BY point_timestamp ASC`,
        [row.id]
      );

      workouts.push({
        workoutId: row.id,
        userId: row.user_id,
        distanceMeters: Number(row.distance_meters),
        durationSeconds: Number(row.duration_seconds),
        averagePaceSecondsPerKm: Number(row.average_pace_seconds_per_km),
        startTimeIso: new Date(row.start_time).toISOString(),
        totalElevation: Number(row.total_elevation),
        notes: row.notes,
        routePoints: pointsResult.rows.map(point => ({
          latitude: Number(point.latitude),
          longitude: Number(point.longitude),
          altitude: Number(point.altitude),
          timestamp: Number(point.point_timestamp)
        }))
      });
    }

    return res.json(workouts);
  } catch (error) {
    console.error('History error:', error);
    return res.status(500).json({ success: false, message: 'Unable to fetch workouts.' });
  }
});

app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ success: false, message: 'Unexpected server error.' });
});

if (require.main === module) {
  initialiseDatabase()
    .then(() => {
      app.listen(port, () => {
        console.log(`SPRINTA API listening on port ${port}`);
      });
    })
    .catch(error => {
      console.error('Database initialisation failed:', error);
      process.exit(1);
    });
}

module.exports = { app, isValidEmail, isValidPassword };
