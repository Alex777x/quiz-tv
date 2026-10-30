package com.clean.quiztv.domain.model

/**
 * Dual-language dictionary (Polish and Russian)
 * Clean editorial typography with zero cartoon emojis.
 */
object Strings {
    fun partyQuiz(@Suppress("UNUSED_PARAMETER") lang: AppLanguage? = null) = "QUIZ TV"

    fun modeBadge(mode: GameMode, lang: AppLanguage): String = when (mode) {
        GameMode.KIDS -> if (lang == AppLanguage.PL) "DZIECI" else "ДЕТИ"
        GameMode.ADULTS -> if (lang == AppLanguage.PL) "DOROŚLI" else "ВЗРОСЛЫЕ"
    }

    fun formatBadge(format: GameFormat, lang: AppLanguage): String = when (format) {
        GameFormat.INDIVIDUAL -> if (lang == AppLanguage.PL) "SOLO" else "СОЛО"
        GameFormat.TEAMS -> if (lang == AppLanguage.PL) "DRUŻYNY" else "КОМАНДЫ"
    }

    fun teamLeaderboardTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYNIKI DRUŻYNOWE" else "КОМАНДНЫЙ ЗАЧЕТ"

    fun teamWinnerTitle(teamName: String, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ZWYCIĘSKA DRUŻYNA: $teamName" else "ПОБЕДИЛА КОМАНДА: $teamName"

    fun scanQr(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Zeskanuj kod QR telefonem" else "Сканируйте QR-код с телефона"

    fun orOpenBrowser(lang: AppLanguage, url: String) =
        if (lang == AppLanguage.PL) "Lub wpisz adres: $url" else "Или откройте в браузере: $url"

    fun step1(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Połącz się z tą samą siecią Wi-Fi" else "Подключитесь к одной сети Wi-Fi"

    fun step2(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Skieruj aparat telefonu na kod QR" else "Наведите камеру смартфона на QR-код"

    fun step3(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Wpisz imię i dołącz do pokoju" else "Введите имя и войдите в комнату"

    fun playersInRoom(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "GRACZE W POKOJU" else "УЧАСТНИКИ В КОМНАТЕ"

    fun waitingForPlayersPrompt(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Oczekiwanie na podłączenie graczy.\nZeskanuj kod QR po lewej stronie." else "Ожидание подключения участников.\nОтсканируйте QR-код на экране."

    fun selectedThemeLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBRANY PAKIET:" else "ВЫБРАННЫЙ ПАКЕТ:"

    fun startGameBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ROZPOCZNIJ GRĘ" else "НАЧАТЬ ИГРУ"

    fun waitingForPlayersBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "OCZEKIWANIE NA GRACZY..." else "ОЖИДАНИЕ ИГРОКОВ..."

    fun vipHostBadge(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "PROWADZĄCY" else "ВЕДУЩИЙ"

    fun questionRound(current: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "PYTANIE $current / $total" else "ВОПРОС $current / $total"

    fun answersReceived(count: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ODPOWIEDZI: $count / $total" else "ОТВЕТОВ: $count / $total"

    fun secondsUnit(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "SEK" else "СЕК"

    fun funFactTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "KOMENTARZ I WYJAŚNIENIE" else "КОММЕНТАРИЙ К ОТВЕТУ"

    fun leaderboardTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "KLASYFIKACJA" else "ТУРНИРНАЯ ТАБЛИЦА"

    fun roundOf(current: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Runda $current z $total" else "Раунд $current из $total"

    fun nextQuestionBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "NASTĘPNE PYTANIE" else "СЛЕДУЮЩИЙ ВОПРОС"

    fun toPodiumBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYNIKI KOŃCOWE" else "ФИНАЛЬНЫЕ ИТОГИ"

    fun quizWinnerTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ZWYCIĘZCA TURNIEJU" else "ПОБЕДИТЕЛЬ ИГРЫ"

    fun congratsChampion(name: String, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Pierwsze miejsce: $name" else "Первое место: $name"

    fun playAgainBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "NOWA GRA" else "НОВАЯ ИГРА"

    fun pointsSuffix(score: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "$score PKT" else "$score ОЧК."

    fun randomMixTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Wielki Mix Turniejowy" else "Большой Турнирный Микс"

    fun randomMixDesc(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "6000 sprawdzonych pytań ze wszystkich kategorii dla dorosłych bez powtórzeń." else "6000 проверенных вопросов из всех взрослых рубрик без повторов."

    fun setupTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "KONFIGURACJA GRY" else " ПАРАМЕТРЫ ИГРЫ".trim()

    fun setupSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Wybierz tryb rozgrywki przed otwarciem pokoju" else "Настройте формат партии перед подключением игроков"

    fun langStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "01 / JĘZYK" else "01 / ЯЗЫК"

    fun modeStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "02 / POZIOM" else "02 / РЕЖИМ"

    fun formatStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "03 / FORMAT" else "03 / ФОРМАТ"

    fun timerStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "04 / CZAS RUNDY" else "04 / ТАЙМЕР РАУНДА"

    fun btnProceedToLobby(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "OTWÓRZ POKÓJ GRY" else "ОТКРЫТЬ КОМНАТУ"

    fun btnBackToSetup(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "PARAMETRY" else "ПАРАМЕТРЫ"

    fun btnChooseTheme(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "PRZEJDŹ DO WYBORU PAKIETU" else "ПЕРЕЙТИ К ВЫБОРУ ПАКЕТА"

    fun themeSelectTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBÓR PAKIETU PYTAŃ" else "ВЫБОР ПАКЕТА ВОПРОСОВ"

    fun themeSelectSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Baza zweryfikowanych pytań turniejowych" else "Выберите рубрику с проверенной базой вопросов"

    fun btnStartQuizNow(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ROZPOCZNIJ PARTIĘ" else "НАЧАТЬ ПАРТИЮ"

    fun btnBackToLobby(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "POWRÓT DO POKOJU" else "НАЗАД В ЛОББИ"

    fun selectedThemeBadge(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "AKTYWNY" else "ВЫБРАН"

    fun scanQrInstruction(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Smartfon pełni rolę kontrolera odpowiedzi" else "Смартфон используется как пульт для ответов"

    fun gamePausedTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "GRA WSTRZYMANA" else "ПАУЗА"

    fun gamePausedSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Rozgrywka została tymczasowo wstrzymana" else "Раунд временно приостановлен"

    fun musicGuessTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "RUNDA MUZYCZNA • ODSŁUCH FRAGMENTU" else "АУДИО-РАУНД • ПРОСЛУШАЙТЕ ФРАГМЕНТ"

    fun rebusGuessTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ZAGADKA WIZUALNA" else "ВИЗУАЛЬНАЯ ЗАГАДКА"

    fun replayMelodyBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "POWTÓRZ DŹWIĘK" else "ПОВТОРИТЬ ЗВУК"
}
