package za.co.sprinta.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.sprinta.app.util.Validators

class ValidatorsTest {
    @Test fun validEmailIsAccepted() = assertTrue(Validators.isValidEmail("runner@example.com"))
    @Test fun invalidEmailIsRejected() = assertFalse(Validators.isValidEmail("runner@"))
    @Test fun strongPasswordIsAccepted() = assertTrue(Validators.isValidPassword("Password@123"))
    @Test fun weakPasswordIsRejected() = assertFalse(Validators.isValidPassword("password"))
}
