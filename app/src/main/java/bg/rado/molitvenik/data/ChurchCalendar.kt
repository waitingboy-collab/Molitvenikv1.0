package bg.rado.molitvenik.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Опростен православен календар (Българска православна църква):
 * - неподвижните празници са по новия стил (григориански);
 * - Великден и подвижните празници се изчисляват по юлианската пасхалия;
 * - постите са опростени (без подробни разпоредби за олио/риба за всеки ден).
 */
object ChurchCalendar {

    data class Feast(val title: String, val names: List<String> = emptyList(), val major: Boolean = false)

    data class Day(val date: LocalDate, val feasts: List<Feast>) {
        val names: List<String> get() = feasts.flatMap { it.names }.distinct()
    }

    /** Православен Великден за дадена година (валидно за 1900–2099). */
    fun easter(year: Int): LocalDate {
        val a = year % 4
        val b = year % 7
        val c = year % 19
        val d = (19 * c + 15) % 30
        val e = (2 * a + 4 * b - d + 34) % 7
        val month = (d + e + 114) / 31
        val day = (d + e + 114) % 31 + 1
        return LocalDate.of(year, month, day).plusDays(13) // юлиански → григориански
    }

    private fun f(title: String, vararg names: String, major: Boolean = false) =
        Feast(title, names.toList(), major)

    // Ключ: "MM-dd"
    private val FIXED: Map<String, List<Feast>> = mapOf(
        "01-01" to listOf(f("Св. Василий Велики (Васильовден)", "Васил", "Василка", "Веселин", "Веселина")),
        "01-06" to listOf(f("Богоявление (Йордановден)", "Йордан", "Йорданка", "Богдан", "Богдана", major = true)),
        "01-07" to listOf(f("Събор на св. Йоан Кръстител (Ивановден)", "Иван", "Иванка", "Йоан", "Йоана", "Ваня")),
        "01-17" to listOf(f("Св. Антоний Велики (Антоновден)", "Антон", "Антония", "Андон")),
        "01-18" to listOf(f("Св. Атанасий Велики (Атанасовден)", "Атанас", "Атанаска", "Наско")),
        "01-20" to listOf(f("Св. Евтимий Велики", "Евтим", "Евтимий")),
        "02-01" to listOf(f("Св. мъченик Трифон (Трифоновден)", "Трифон")),
        "02-02" to listOf(f("Сретение Господне")),
        "02-10" to listOf(f("Св. Харалампий", "Харалампи")),
        "03-09" to listOf(f("Св. 40 мъченици", "Младен", "Младена")),
        "03-25" to listOf(f("Благовещение", "Благой", "Блага", "Благовест", "Благовеста", "Евангелина", major = true)),
        "05-06" to listOf(f("Св. великомъченик Георги (Гергьовден)", "Георги", "Гергана", "Галина", "Генка")),
        "05-11" to listOf(f("Св. св. Кирил и Методий", "Кирил", "Методий")),
        "05-21" to listOf(f("Св. св. Константин и Елена", "Константин", "Костадин", "Елена", "Еленка")),
        "06-24" to listOf(f("Рождество на св. Йоан Кръстител (Еньовден)", "Еньо", "Яна")),
        "06-29" to listOf(f("Св. св. апостоли Петър и Павел (Петровден)", "Петър", "Петя", "Павел", "Павлина")),
        "07-17" to listOf(f("Св. великомъченица Марина", "Марина")),
        "07-20" to listOf(f("Св. пророк Илия (Илинден)", "Илия", "Илиян", "Илияна")),
        "08-06" to listOf(f("Преображение Господне", major = true)),
        "08-15" to listOf(f("Успение Богородично (Голяма Богородица)", "Мария", "Мариана", "Мара", "Марийка", major = true)),
        "08-29" to listOf(f("Отсичане главата на св. Йоан Кръстител")),
        "09-08" to listOf(f("Рождество Богородично (Малка Богородица)", major = true)),
        "09-14" to listOf(f("Въздвижение на Светия Кръст (Кръстовден)", "Кръстьо", "Кръстина", "Ставри", major = true)),
        "09-17" to listOf(f("Св. мъченици Вяра, Надежда и Любов и майка им София", "Вяра", "Надежда", "Любов", "Люба", "София")),
        "10-14" to listOf(f("Св. Петка Българска (Петковден)", "Петко", "Петкана", "Параскева")),
        "10-19" to listOf(f("Св. Йоан Рилски Чудотворец", "Иван")),
        "10-26" to listOf(f("Св. великомъченик Димитър (Димитровден)", "Димитър", "Димитрина", "Димо", "Митко")),
        "11-08" to listOf(f("Събор на св. Архангел Михаил (Архангеловден)", "Михаил", "Михаела", "Гаврил", "Ангел", "Ангелина", "Рафаил")),
        "11-21" to listOf(f("Въведение Богородично", major = true)),
        "11-30" to listOf(f("Св. апостол Андрей (Андреевден)", "Андрей", "Андреа", "Първан")),
        "12-04" to listOf(f("Св. великомъченица Варвара", "Варвара")),
        "12-05" to listOf(f("Св. Сава Освещени", "Сава")),
        "12-06" to listOf(f("Св. Николай Чудотворец (Никулден)", "Никола", "Николай", "Николина", "Нико")),
        "12-09" to listOf(f("Св. Анна", "Анна", "Ана")),
        "12-20" to listOf(f("Св. Игнатий Богоносец (Игнажден)", "Игнат", "Игнатий")),
        "12-24" to listOf(f("Бъдни вечер")),
        "12-25" to listOf(f("Рождество Христово (Коледа)", "Христо", "Христина", "Кристиан", "Кристина", "Емануил", major = true)),
        "12-26" to listOf(f("Събор на Пресвета Богородица")),
        "12-27" to listOf(f("Св. първомъченик Стефан (Стефановден)", "Стефан", "Стефка", "Стефания", "Венцислав")),
    )

    // Ключ: брой дни спрямо Великден
    private val MOVABLE: Map<Int, List<Feast>> = mapOf(
        -70 to listOf(f("Неделя на Митаря и фарисея")),
        -57 to listOf(f("Месопустна задушница")),
        -56 to listOf(f("Месни заговезни")),
        -49 to listOf(f("Сирни заговезни (Прошка)")),
        -48 to listOf(f("Чист понеделник — начало на Великия пост")),
        -43 to listOf(f("Тодоровден", "Тодор", "Теодор", "Теодора", "Тодорка", "Божидар", "Божидара")),
        -8 to listOf(f("Лазаровден", "Лазар", "Лазарина")),
        -7 to listOf(f("Вход Господен в Йерусалим (Цветница)", "Цветан", "Цветана", "Цветелина", "Цветомир", "Лиляна", "Роза", "Виолета", "Явор", "Върбан", major = true)),
        -3 to listOf(f("Велики четвъртък")),
        -2 to listOf(f("Велики петък (Разпети петък)", major = true)),
        -1 to listOf(f("Велика събота")),
        0 to listOf(f("Възкресение Христово — Великден", major = true)),
        1 to listOf(f("Светли понеделник")),
        7 to listOf(f("Томина неделя")),
        39 to listOf(f("Възнесение Господне (Спасовден)", "Спас", "Спаска", major = true)),
        48 to listOf(f("Черешова задушница")),
        49 to listOf(f("Петдесетница", major = true)),
        50 to listOf(f("Ден на Светия Дух")),
        56 to listOf(f("Неделя на всички светии")),
    )

    private fun key(d: LocalDate) = "%02d-%02d".format(d.monthValue, d.dayOfMonth)

    private fun archangelSaturday(year: Int): LocalDate {
        var s = LocalDate.of(year, 11, 7)
        while (s.dayOfWeek != DayOfWeek.SATURDAY) s = s.minusDays(1)
        return s
    }

    fun day(date: LocalDate): Day {
        val list = mutableListOf<Feast>()
        val offset = ChronoUnit.DAYS.between(easter(date.year), date).toInt()
        MOVABLE[offset]?.let { list += it }
        FIXED[key(date)]?.let { list += it }
        if (date == archangelSaturday(date.year)) list += f("Архангелова задушница")
        return Day(date, list)
    }

    /** Опростено указание за поста в деня. */
    fun fast(date: LocalDate): String {
        val easter = easter(date.year)
        val d = ChronoUnit.DAYS.between(easter, date).toInt()
        val md = date.monthValue * 100 + date.dayOfMonth
        val dow = date.dayOfWeek

        if (md >= 1225 || md <= 104) return "Блажен ден (Святки)"
        if (d in 0..6) return "Светла седмица — блажен ден"
        if (d in 50..55) return "Седмицата след Петдесетница — блажен ден"
        if (d in -69..-64) return "Блажен ден (седмица без пост)"
        if (d in -55..-49) return "Сирна седмица — без месо"
        if (d in -48..-1) {
            return if (d == -7 || md == 325) "Великият пост — разрешава се риба" else "Великият пост — постен ден"
        }
        val petrovStart = easter.plusDays(57)
        if (!date.isBefore(petrovStart) && !date.isAfter(LocalDate.of(date.year, 6, 28))) return "Петров пост"
        if (md in 801..814) return if (md == 806) "Богородичен пост — разрешава се риба" else "Богородичен пост"
        if (md in 1115..1224) return "Коледни пости"
        if (md == 105 || md == 829 || md == 914) return "Постен ден"
        if (dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.FRIDAY) return "Постен ден (сряда и петък)"
        return "Блажен ден"
    }

    /** Следващите дни с празници (от [from] включително). */
    fun upcoming(from: LocalDate, days: Int): List<Day> =
        (0 until days).map { day(from.plusDays(it.toLong())) }.filter { it.feasts.isNotEmpty() }

    /** Следващият голям празник след днес. */
    fun nextMajor(from: LocalDate): Day? =
        (1..400).asSequence()
            .map { day(from.plusDays(it.toLong())) }
            .firstOrNull { d -> d.feasts.any { it.major } }

    /** Кога е имен ден на човек с това име (до една година напред). */
    fun findName(query: String, from: LocalDate): List<Pair<Day, String>> {
        val q = Search.normalize(query)
        if (q.length < 2) return emptyList()
        val result = mutableListOf<Pair<Day, String>>()
        for (i in 0..366) {
            val d = day(from.plusDays(i.toLong()))
            d.names.firstOrNull { Search.normalize(it).startsWith(q) }?.let { result += d to it }
        }
        return result
    }
}
