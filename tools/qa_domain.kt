import com.sanad.anklerehab.domain.*
import java.time.LocalDate

fun main() {
    check(PlanCatalog.TOTAL_PROGRAM_DAYS == 42)
    check(PlanCatalog.totalTrainingDays(6) == 34)
    check(PlanCatalog.totalTrainingDays(5) == 32)
    check(!PlanCatalog.day(7).trainingDay)
    check(PlanCatalog.day(1).exercises.map { it.key }.containsAll(listOf("TOWEL_CURL_1", "TOWEL_CURL_2")))

    val start = LocalDate.of(2026, 9, 1)
    val base = RehabSettings(startDate = start)
    check(ScheduleEngine.state(start, base).todayProgramDay == 1)
    check(ScheduleEngine.state(start.plusDays(13), base).todayProgramDay == 14)
    check(ScheduleEngine.state(start.plusDays(14), base).transitionPendingToPhase == 2)
    check(!ScheduleEngine.canEditSession(2, start, base))
    check(ScheduleEngine.canEditSession(1, start, base))

    val p2 = start.plusDays(20)
    val s2 = base.copy(phase2StartDate = p2)
    check(ScheduleEngine.state(p2, s2).todayProgramDay == 15)
    check(ScheduleEngine.state(p2.plusDays(14), s2).transitionPendingToPhase == 3)

    val p3 = p2.plusDays(20)
    val s3 = s2.copy(phase3StartDate = p3)
    check(ScheduleEngine.state(p3, s3).todayProgramDay == 29)
    check(ScheduleEngine.state(p3.plusDays(14), s3).programComplete)

    val plan = PlanCatalog.day(1)
    val all = plan.exercises.mapTo(linkedSetOf()) { it.key }
    check(SessionPolicy.allRequiredChecked(plan, all))
    all.remove("TOWEL_CURL_2")
    check(!SessionPolicy.allRequiredChecked(plan, all))

    println("DOMAIN_QA_OK")
}
