package com.sanad.anklerehab.domain

object PlanCatalog {
    const val TOTAL_PROGRAM_DAYS = 42

    fun day(programDay: Int, phase2DaysPerWeek: Int = 6): DayPlan {
        require(programDay in 1..TOTAL_PROGRAM_DAYS)
        val week = ((programDay - 1) / 7) + 1
        val dayInWeek = ((programDay - 1) % 7) + 1
        return when (week) {
            1, 2 -> phase1(programDay, week, dayInWeek)
            3, 4 -> phase2(programDay, week, dayInWeek, phase2DaysPerWeek.coerceIn(5, 6))
            else -> phase3(programDay, week, dayInWeek)
        }
    }

    fun totalTrainingDays(phase2DaysPerWeek: Int = 6): Int =
        (1..TOTAL_PROGRAM_DAYS).count { day(it, phase2DaysPerWeek).trainingDay }

    fun phaseRange(phase: Int): IntRange = when (phase) {
        1 -> 1..14
        2 -> 15..28
        3 -> 29..42
        else -> error("Unknown phase $phase")
    }

    private fun phase1(programDay: Int, week: Int, dayInWeek: Int): DayPlan {
        val training = dayInWeek <= 6
        val exercises = if (!training) emptyList() else listOf(
            ExerciseStep(
                key = "PREP_TOES_60",
                title = "تهيئة الأنسجة",
                instruction = "اجلس بوضع مريح مع مد الساق أمامك وحرّك أصابع القدم.",
                dose = "1 دقيقة",
                section = ExerciseSection.PREP,
                timerSeconds = 60
            ),
            ExerciseStep(
                key = "ANKLE_CIRCLES",
                title = "دوائر الكاحل",
                instruction = "ارسم دوائر واسعة وبطيئة بمفصل الكاحل في الهواء.",
                dose = "10 مرات لكل اتجاه",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "TOWEL_CURL_1",
                title = "كرشمة الفوطة — المرة 1",
                instruction = "ضع فوطة صغيرة على الأرض واسحبها بأصابع القدم نحو الداخل.",
                dose = "10 مرات",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "ISOMETRIC_EVERSION",
                title = "المقاومة الثابتة الخفيفة",
                instruction = "ادفع القدم نحو الخارج برفق ضد قاعدة ثابتة أو حائط دون تحريك المفصل.",
                dose = "10 عدات · ثبات 5 ثوانٍ لكل ضغطة",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "TOWEL_CURL_2",
                title = "كرشمة الفوطة — المرة 2",
                instruction = "كرر كرشمة الفوطة في وقت آخر من اليوم.",
                dose = "10 مرات",
                section = ExerciseSection.EXTRA
            ),
            ExerciseStep(
                key = "ELEVATE_10_MIN",
                title = "رفع القدم بعد الجلسة",
                instruction = "ارفع القدم المصابة على وسائد أعلى من مستوى القلب.",
                dose = "10 دقائق",
                section = ExerciseSection.RECOVERY,
                timerSeconds = 600
            )
        )
        return DayPlan(
            programDay = programDay,
            week = week,
            dayInWeek = dayInWeek,
            phase = 1,
            trainingDay = training,
            phaseTitle = "فك التيبس وتنشيط الدورة الدموية",
            phaseSubtitle = "الأسبوع $week من 6 · 6 أيام تدريب أسبوعيًا",
            exercises = exercises,
            walkingGuidance = "استخدم العكاز في اليد اليسرى المعاكسة للرجل اليمنى المصابة، وحرّك العكاز مع الرجل المصابة في اللحظة نفسها."
        )
    }

    private fun phase2(programDay: Int, week: Int, dayInWeek: Int, daysPerWeek: Int): DayPlan {
        val training = dayInWeek <= daysPerWeek
        val exercises = if (!training) emptyList() else listOf(
            ExerciseStep(
                key = "PREP_TOES_60",
                title = "تهيئة الأنسجة",
                instruction = "اجلس بوضع مريح مع مد الساق أمامك وحرّك أصابع القدم.",
                dose = "1 دقيقة",
                section = ExerciseSection.PREP,
                timerSeconds = 60
            ),
            ExerciseStep(
                key = "ANKLE_CIRCLES",
                title = "دوائر الكاحل الخفيفة",
                instruction = "حرّك الكاحل في دوائر خفيفة لتليين الأنسجة.",
                dose = "10 مرات",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "LATERAL_RESISTANCE",
                title = "المقاومة الجانبية",
                instruction = "استخدم طرحة قطنية متينة أو شريطًا مطاطيًا واضغط بالقدم نحو الخارج.",
                dose = "10 مرات × 3 مجموعات",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "SUPPORTED_PARTIAL_WEIGHT",
                title = "الوقوف الجزئي الداعم",
                instruction = "استند بكلتا اليدين على الحائط وانقل وزنًا ضئيلًا جدًا على الرجل المصابة، ثم زِد المدة تدريجيًا أسبوعيًا.",
                dose = "2–3 ثوانٍ مبدئيًا",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "ELEVATE_10_MIN",
                title = "رفع القدم بعد الجلسة",
                instruction = "ارفع القدم المصابة على وسائد أعلى من مستوى القلب.",
                dose = "10 دقائق",
                section = ExerciseSection.RECOVERY,
                timerSeconds = 600
            )
        )
        return DayPlan(
            programDay = programDay,
            week = week,
            dayInWeek = dayInWeek,
            phase = 2,
            trainingDay = training,
            phaseTitle = "المقاومة وبدء التحميل التدريجي",
            phaseSubtitle = "الأسبوع $week من 6 · $daysPerWeek أيام تدريب أسبوعيًا",
            exercises = exercises,
            walkingGuidance = "قلّل الاعتماد على العكاز تدريجيًا داخل المنزل مع زيادة ثبات الخطوات، وبقدر ما تسمح به قدرتك وتوجيه المختص."
        )
    }

    private fun phase3(programDay: Int, week: Int, dayInWeek: Int): DayPlan {
        val training = dayInWeek <= 5
        val exercises = if (!training) emptyList() else listOf(
            ExerciseStep(
                key = "PREP_TOES_60",
                title = "تهيئة الأنسجة",
                instruction = "اجلس بوضع مريح مع مد الساق أمامك وحرّك أصابع القدم.",
                dose = "1 دقيقة",
                section = ExerciseSection.PREP,
                timerSeconds = 60
            ),
            ExerciseStep(
                key = "FLEX_EXTEND",
                title = "الإحماء الحركي",
                instruction = "اثنِ وابسط القدم للأمام والخلف ببطء.",
                dose = "15 مرة",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "SINGLE_LEG_BALANCE",
                title = "الثبات الفردي",
                instruction = "حاول الوقوف على الرجل المصابة مع استناد خفيف بإصبع اليد على الحائط للأمان، وزِد الثبات تدريجيًا.",
                dose = "من 5 إلى 10 ثوانٍ",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "BAND_EVERSION_REPEAT",
                title = "المقاومة بالشريط للخارج",
                instruction = "كرر تمرين الشريط المطاطي للخارج للحفاظ على قوة عضلات التوازن.",
                dose = "نفس جرعة المرحلة السابقة",
                section = ExerciseSection.MAIN
            ),
            ExerciseStep(
                key = "ELEVATE_10_MIN",
                title = "رفع القدم بعد الجلسة",
                instruction = "ارفع القدم المصابة على وسائد أعلى من مستوى القلب.",
                dose = "10 دقائق",
                section = ExerciseSection.RECOVERY,
                timerSeconds = 600
            )
        )
        return DayPlan(
            programDay = programDay,
            week = week,
            dayInWeek = dayInWeek,
            phase = 3,
            trainingDay = training,
            phaseTitle = "الثبات والعودة التدريجية للأنشطة",
            phaseSubtitle = "الأسبوع $week من 6 · 5 أيام تدريب أسبوعيًا",
            exercises = exercises,
            walkingGuidance = "المشي المستقل بثبات، مع محاولة وضع كف القدم مستويًا على الأرض لتقليل الميل جهة الإصبع الصغير."
        )
    }
}
