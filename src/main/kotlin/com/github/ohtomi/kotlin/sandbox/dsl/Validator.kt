package com.github.ohtomi.kotlin.sandbox.dsl

interface Validatable {

    val name: String
    val value: String

    fun validate(option: ValidateOption = ValidateOption(ValidateLevel.Error),
                 create: Validators.() -> Validator): ValidateResult {
        val validator = Validators.create()
        return validator.validate(this.name, this.value, option)
    }
}

sealed class ValidateLevel {

    object Error : ValidateLevel()
    object Warning : ValidateLevel()
}

data class ValidateOption(val level: ValidateLevel)

data class ValidateResult(val errors: List<String>, val warnings: List<String>) {

    operator fun plus(other: ValidateResult): ValidateResult {
        return ValidateResult(
                errors.union(other.errors).toList(),
                warnings.union(other.warnings).toList()
        )
    }
}

interface Validator {

    fun validate(name: String, value: String, option: ValidateOption): ValidateResult
}

class ValidatorAnd(val v1: Validator, val v2: Validator) : Validator {

    override fun validate(name: String, value: String, option: ValidateOption): ValidateResult {
        val vr1 = v1.validate(name, value, option)
        val vr2 = v2.validate(name, value, option)
        return vr1 + vr2
    }
}

infix fun Validator.and(other: Validator): Validator = ValidatorAnd(this, other)

class ValidatorOr(val v1: Validator, val v2: Validator) : Validator {

    override fun validate(name: String, value: String, option: ValidateOption): ValidateResult {
        val vr1 = v1.validate(name, value, option)
        if (vr1.errors.isNotEmpty()) {
            return vr1
        }
        if (vr1.warnings.isNotEmpty() && option.level == ValidateLevel.Warning) {
            return vr1
        }
        val vr2 = v2.validate(name, value, option)
        return vr1 + vr2
    }
}

infix fun Validator.or(other: Validator): Validator = ValidatorOr(this, other)

object Validators {

    fun mandatory(): Validator = object : Validator {
        override fun validate(name: String, value: String, option: ValidateOption): ValidateResult {
            return if (value.isNotEmpty()) {
                ValidateResult(emptyList(), emptyList())
            } else {
                ValidateResult(listOf("$name must be not null."), emptyList())
            }
        }
    }

    fun maxLength(upper: Int): Validator = object : Validator {
        override fun validate(name: String, value: String, option: ValidateOption): ValidateResult {
            return if (value.length <= upper) {
                ValidateResult(emptyList(), emptyList())
            } else {
                ValidateResult(listOf("$name must be $upper characters or less."), emptyList())
            }
        }
    }

    fun minLength(lower: Int): Validator = object : Validator {
        override fun validate(name: String, value: String, option: ValidateOption): ValidateResult {
            return if (value.length >= lower) {
                ValidateResult(emptyList(), emptyList())
            } else {
                ValidateResult(emptyList(), listOf("$name must be $lower characters or more."))
            }
        }
    }
}
