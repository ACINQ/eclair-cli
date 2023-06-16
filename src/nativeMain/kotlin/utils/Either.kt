package utils

sealed class Either<out L, out R> {
    abstract val isLeft: Boolean
    abstract val isRight: Boolean
    abstract val left: L?
    abstract val right: R?

    inline fun <X> fold(fl: (L) -> X, fr: (R) -> X): X = when (this) {
        is Left -> fl(this.value)
        is Right -> fr(this.value)
    }

    inline fun <X, Y> transform(fl: (L) -> X, fr: (R) -> Y): Either<X, Y> = when (this) {
        is Left -> Left(fl(this.value))
        is Right -> Right(fr(this.value))
    }

    inline fun <X> map(f: (R) -> X): Either<L, X> = transform({ it }, f)

    data class Left<out L, Nothing>(val value: L) : Either<L, Nothing>() {
        override val isLeft: Boolean = true
        override val isRight: Boolean = false
        override val left: L? = value
        override val right: Nothing? = null
    }

    data class Right<Nothing, out R>(val value: R) : Either<Nothing, R>() {
        override val isLeft: Boolean = false
        override val isRight: Boolean = true
        override val left: Nothing? = null
        override val right: R? = value
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <L, R, X> Either<L, R>.flatMap(f: (R) -> Either<L, X>): Either<L, X> = when (this) {
    is Either.Left -> this as Either<L, X>
    is Either.Right -> f(this.value)
}

inline fun <L, R> Either<L, R>.getOrElse(onLeft: (L) -> R): R = when (this) {
    is Either.Left -> onLeft(this.value)
    is Either.Right -> this.value
}

fun <L, R> Either<L, R>.getOrDefault(defaultValue: R): R = when (this) {
    is Either.Left -> defaultValue
    is Either.Right -> this.value
}
