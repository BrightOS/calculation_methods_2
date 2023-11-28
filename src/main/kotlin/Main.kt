import java.math.BigDecimal
import kotlin.math.*

private var a = 0.0.toBigDecimal()
private var b = PI.toBigDecimal()
private val xList = arrayListOf<BigDecimal>()
private val yList = arrayListOf<BigDecimal>()
private val mList = arrayListOf<BigDecimal>()
private val maxDeltas = mutableMapOf<Int, BigDecimal>()

// Функция
fun func(x: BigDecimal) = sin(x.toDouble())

// Первая производная от функции
fun func1(x: BigDecimal) = cos(x.toDouble())

// Вторая производная от функции
fun func2(x: BigDecimal) = -sin(x.toDouble())

operator fun Double.invoke() = this.toBigDecimal()

// Метод прогонки
fun persecution(n: Int) {
    val h = (b - a) / n.toBigDecimal()

    val aList = arrayListOf<BigDecimal>()
    val bList = arrayListOf<BigDecimal>()
    val cList = arrayListOf<BigDecimal>()
    val dList = arrayListOf<BigDecimal>()

    val lList = arrayListOf<BigDecimal>()
    val muList = arrayListOf<BigDecimal>()

    // Инициализация a, b, c, d (Пункт 4.2)

    aList.add(1.0())
    bList.add(0.0())
    cList.add(0.0())
    dList.add(func2(a)())

    for (i in 1 until n) {
        aList.add(((2).toBigDecimal() * h / (3).toBigDecimal()))
        bList.add(h / (6).toBigDecimal())
        cList.add(h / (6).toBigDecimal())
        dList.add(((yList[i + 1] - yList[i]) / h) - ((yList[i] - yList[i - 1]) / h))
    }
    aList.add(BigDecimal.ONE)
    bList.add(BigDecimal.ZERO)
    cList.add(BigDecimal.ZERO)
    dList.add(func2(b)())

    // Инициализация прогоночных коэффициентов (Пункт 4.2, 2.4.16)

    lList.add(-bList[0] / aList[0])
    muList.add(dList[0] / aList[0])

    for (i in 1..n) {
        lList.add(-(bList[i] / (aList[i] + cList[i] * lList[i - 1])))
        muList.add((dList[i] - (cList[i] * muList[i - 1])) / (aList[i] + (cList[i] * lList[i - 1])))
    }

    // Поиск решения системы (2.4.15)

    mList.clear()
    mList.add(muList[n])
    for (i in n - 1 downTo 0) {
        mList.add(lList[i] * mList.last() + muList[i])
    }
    mList.reverse()
}

// Вычисление значений функции S3(x) (2.4.13)
fun spline(x: BigDecimal, n: Int): BigDecimal {
    val h = ((b - a) / n.toBigDecimal())
    val i = ((x - a) / h + BigDecimal.ONE).toInt()
    val t1 = (((xList[i] - x).pow(3) - h * h * (xList[i] - x)) / (6.0.toBigDecimal() * h)) * mList[i - 1]
    val t2 = (((x - xList[i - 1]).pow(3) - h * h * (x - xList[i - 1])) / (6.0.toBigDecimal() * h)) * mList[i]
    val t3 = (xList[i] - x) * yList[i - 1] / h
    val t4 = (x - xList[i - 1]) * yList[i] / h
    val t = t1 + t2 + t3 + t4
    return t
}

// Равномерное разбиение на n частей размера h, заполнение x, y
fun breakdown(n: Int) {
    val h = (b - a) / (n).toBigDecimal()

    xList.clear()
    yList.clear()
    xList.add(a)
    yList.add(func(xList[0])())

    for (i in 1..n) {
        xList.add(xList.last() + h)
        yList.add(func(xList[i])())
    }
}

// Вычисление максимальной погрешности
fun maxDelta(n: Int): BigDecimal {
    val h = (b - a) / (n).toBigDecimal()

    breakdown(n)
    persecution(n)

    var maxDelta = BigDecimal.ZERO
    // Формула 2.5.2 численного эксперимента
    for (i in 0 until n) {
        val v = spline(xList[i] + h / (2).toBigDecimal(), n)
        val delta = (func(xList[i] + h / (2).toBigDecimal())() - v).abs()
        if (delta > maxDelta)
            maxDelta = delta
    }

    return maxDelta
}

// Оценочная максимальная погрешность
fun estimateDelta(n: Int) = (maxDeltas[n / 2] ?: BigDecimal.ZERO) / BigDecimal(16) // 2^k, k = 4, k -

// Фактический коэффициент уменьшения максимальной погрешности
fun kDelta(n: Int) = (maxDeltas[n / 2] ?: BigDecimal.ZERO) / (maxDeltas[n] ?: BigDecimal.ZERO)

fun main() {
    var n = 5 // Число делений области интегрирования
    println("N     \t| Dmax  \t| D-oc  \t| Kd")
    while (n <= 10240) {
        maxDeltas.put(n, maxDelta(n))
        println(
            "$n  \t| ${String.format("%.2e", maxDeltas[n])}\t| ${
                String.format(
                    "%.2e",
                    estimateDelta(n)
                )
            }\t| ${String.format("%.2f", kDelta(n))}"
        )
        n *= 2
    }
}