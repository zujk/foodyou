package com.maksimowiczm.foodyou.tdee.domain.usecase

import com.maksimowiczm.foodyou.tdee.domain.entity.GoalOutcome
import com.maksimowiczm.foodyou.tdee.domain.entity.WeightTrend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeightProjectorTest {

    @Test
    fun deficitOfOneEnergyDensityPerDayLosesOneKgPerDay() {
        // Degenerate but exact: a 7000 kcal deficit over a 7000 kcal/kg density is 1 kg per day.
        val projection =
            WeightProjector.project(
                currentWeightKg = 100.0,
                maintenanceKcal = 7000.0,
                dailyIntakeKcal = 0.0,
                energyDensityKcalPerKg = 7000.0,
            )

        assertEquals(-1.0, projection.weightChangeKgPerDay, TOLERANCE)
        assertEquals(-7.0, projection.weightChangeKgPerWeek, TOLERANCE)
    }

    @Test
    fun realisticDeficitLosesHalfAKgPerWeek() {
        // 500 kcal/day * 7 days = 3500 kcal; 3500 / 7000 = 0.5 kg per week.
        val projection =
            WeightProjector.project(
                currentWeightKg = 80.0,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2000.0,
                energyDensityKcalPerKg = 7000.0,
            )

        assertEquals(-500.0, projection.dailyBalanceKcal, TOLERANCE)
        assertEquals(-0.5, projection.weightChangeKgPerWeek, TOLERANCE)
        assertEquals(WeightTrend.Loss, projection.trend)
    }

    @Test
    fun surplusGainsWeight() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 60.0,
                maintenanceKcal = 2000.0,
                dailyIntakeKcal = 2500.0,
                energyDensityKcalPerKg = 7000.0,
            )

        assertEquals(0.5, projection.weightChangeKgPerWeek, TOLERANCE)
        assertEquals(WeightTrend.Gain, projection.trend)
    }

    @Test
    fun intakeMatchingExpenditureIsMaintenance() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 70.0,
                maintenanceKcal = 2200.0,
                dailyIntakeKcal = 2200.0,
            )

        assertEquals(0.0, projection.weightChangeKgPerDay, TOLERANCE)
        assertEquals(WeightTrend.Maintenance, projection.trend)
        projection.milestones.forEach { assertEquals(70.0, it.projectedWeightKg, TOLERANCE) }
    }

    @Test
    fun milestonesFollowTheDailyRate() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 90.0,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2000.0,
                energyDensityKcalPerKg = 7000.0,
            )

        val thirtyDays = projection.milestones.single { it.days == 30 }

        // 500 kcal * 30 days / 7000 = 2.142857 kg
        assertEquals(-2.142857, thirtyDays.changeKg, TOLERANCE)
        assertEquals(87.857143, thirtyDays.projectedWeightKg, TOLERANCE)
    }

    @Test
    fun goalIsReachedInTheExpectedNumberOfDays() {
        // 10 kg to lose at 500 kcal/day: 10 * 7000 / 500 = 140 days.
        val projection =
            WeightProjector.project(
                currentWeightKg = 90.0,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2000.0,
                targetWeightKg = 80.0,
                energyDensityKcalPerKg = 7000.0,
            )

        val goal = assertIs<GoalOutcome.Reachable>(projection.goal)

        assertEquals(140, goal.days)
        assertEquals(20.0, goal.weeks, TOLERANCE)
    }

    @Test
    fun milestonesStopAtTheGoal() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 90.0,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2000.0,
                targetWeightKg = 80.0,
                energyDensityKcalPerKg = 7000.0,
            )

        // The goal lands on day 140, so the 180 and 365 day milestones are dropped.
        assertTrue(projection.milestones.all { it.days <= 140 })
        assertEquals(listOf(7, 14, 30, 60, 90), projection.milestones.map { it.days })
    }

    @Test
    fun goalAlreadyReached() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 80.02,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2000.0,
                targetWeightKg = 80.0,
            )

        assertIs<GoalOutcome.AlreadyReached>(projection.goal)
    }

    @Test
    fun surplusCannotReachALowerTarget() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 90.0,
                maintenanceKcal = 2000.0,
                dailyIntakeKcal = 2500.0,
                targetWeightKg = 80.0,
            )

        val goal = assertIs<GoalOutcome.Unreachable>(projection.goal)

        assertEquals(GoalOutcome.Unreachable.Reason.WrongDirection, goal.reason)
    }

    @Test
    fun maintenanceNeverReachesATarget() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 90.0,
                maintenanceKcal = 2200.0,
                dailyIntakeKcal = 2200.0,
                targetWeightKg = 80.0,
            )

        val goal = assertIs<GoalOutcome.Unreachable>(projection.goal)

        assertEquals(GoalOutcome.Unreachable.Reason.Maintenance, goal.reason)
    }

    @Test
    fun aTargetTooFarOutIsReportedAsBeyondTheHorizon() {
        // 60 kcal/day is 0.00857 kg/day, so 50 kg takes about 5800 days.
        val projection =
            WeightProjector.project(
                currentWeightKg = 150.0,
                maintenanceKcal = 2500.0,
                dailyIntakeKcal = 2440.0,
                targetWeightKg = 100.0,
                energyDensityKcalPerKg = 7000.0,
            )

        val goal = assertIs<GoalOutcome.Unreachable>(projection.goal)

        assertEquals(GoalOutcome.Unreachable.Reason.BeyondHorizon, goal.reason)
    }

    @Test
    fun noTargetMeansNoGoalOutcome() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 70.0,
                maintenanceKcal = 2200.0,
                dailyIntakeKcal = 1800.0,
            )

        assertNull(projection.goal)
    }

    @Test
    fun projectedWeightNeverGoesBelowOneKg() {
        val projection =
            WeightProjector.project(
                currentWeightKg = 60.0,
                maintenanceKcal = 3000.0,
                dailyIntakeKcal = 0.0,
                energyDensityKcalPerKg = 7000.0,
            )

        assertTrue(projection.milestones.all { it.projectedWeightKg >= 1.0 })
    }

    @Test
    fun energyDensityChangesTheRate() {
        fun rate(density: Double) =
            WeightProjector.project(
                    currentWeightKg = 80.0,
                    maintenanceKcal = 2500.0,
                    dailyIntakeKcal = 2000.0,
                    energyDensityKcalPerKg = density,
                )
                .weightChangeKgPerWeek

        // A denser kilogram takes longer to shed.
        assertTrue(rate(7700.0) > rate(7000.0))
    }

    private companion object {
        const val TOLERANCE = 0.0001
    }
}
