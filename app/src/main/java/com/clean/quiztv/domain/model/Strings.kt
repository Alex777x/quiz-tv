package com.clean.quiztv.domain.model

/**
 * Dual-language dictionary (Polish 🇵🇱 and Russian 🇷🇺)
 * Used across Android TV Compose screens and server-side templates.
 */
object Strings {
    fun partyQuiz(@Suppress("UNUSED_PARAMETER") lang: AppLanguage? = null) = "PARTY QUIZ"

    fun modeBadge(mode: GameMode, lang: AppLanguage): String = when (mode) {
        GameMode.KIDS -> if (lang == AppLanguage.PL) "🎈 DZIECI" else "🎈 ДЕТИ"
        GameMode.ADULTS -> if (lang == AppLanguage.PL) "🍸 DOROŚLI" else "🍸 ВЗРОСЛЫЕ"
    }

    fun formatBadge(format: GameFormat, lang: AppLanguage): String = when (format) {
        GameFormat.INDIVIDUAL -> if (lang == AppLanguage.PL) "👤 INDYWIDUALNY" else "👤 СОЛО"
        GameFormat.TEAMS -> if (lang == AppLanguage.PL) "👥 DRUŻYNY" else "👥 КОМАНДЫ"
    }

    fun teamLeaderboardTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYNIKI DRUŻYNOWE" else "КОМАНДНЫЙ ЗАЧЕТ"

    fun teamWinnerTitle(teamName: String, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYGRYWA DRUŻYNA: $teamName! 🏆" else "ПОБЕДИЛА КОМАНДА: $teamName! 🏆"

    fun scanQr(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Zeskanuj kod QR telefonem" else "Сканируй QR с телефона"

    fun orOpenBrowser(lang: AppLanguage, url: String) =
        if (lang == AppLanguage.PL) "Lub otwórz w przeglądarce: $url" else "Или открой в браузере: $url"

    fun step1(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Połącz się z tym samym Wi-Fi" else "Подключись к той же Wi-Fi сети"

    fun step2(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Skieruj aparat telefonu na QR" else "Наведи камеру смартфона на QR"

    fun step3(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Wybierz nick, awatar i dołącz" else "Выбери ник, аватар и нажми «Войти»"

    fun playersInRoom(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "GRACZE W POKOJU" else "ИГРОКИ В КОМНАТЕ"

    fun waitingForPlayersPrompt(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Tutaj pojawią się gracze\npo zeskanowaniu kodu QR" else "Здесь появятся игроки\nпосле сканирования QR-кода"

    fun selectedThemeLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBRANY TEMAT:" else "ВЫБРАННАЯ ТЕМА:"

    fun startGameBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ROZPOCZNIJ GRĘ" else "НАЧАТЬ ИГРУ"

    fun waitingForPlayersBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "CZEKAMY NA GRACZY..." else "ЖДЕМ ИГРОКОВ..."

    fun vipHostBadge(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "👑 PROWADZĄCY" else "👑 ВЕДУЩИЙ"

    fun questionRound(current: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "PYTANIE $current z $total" else "ВОПРОС $current / $total"

    fun answersReceived(count: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Odpowiedzi: $count / $total" else "Ответов: $count / $total"

    fun secondsUnit(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "SEK" else "СЕК"

    fun funFactTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "CIEKAWOSTKA" else "ИНТЕРЕСНЫЙ ФАКТ"

    fun leaderboardTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "TABELA LIDERÓW" else "ТАБЛИЦА ЛИДЕРОВ"

    fun roundOf(current: Int, total: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Runda $current z $total" else "Раунд $current из $total"

    fun nextQuestionBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "DALEJ" else "ДАЛЕЕ"

    fun toPodiumBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "DO PODIUM" else "К ПОДИУМУ"

    fun quizWinnerTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ZWYCIĘZCA QUIZU! 🏆" else "ПОБЕДИТЕЛЬ КВИЗА! 🏆"

    fun congratsChampion(name: String, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Gratulacje dla mistrza: $name" else "Поздравляем чемпиона: $name"

    fun playAgainBtn(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ZAGRAJ PONOWNIE" else "СЫГРАТЬ ЕЩЕ РАЗ"

    fun pointsSuffix(score: Int, lang: AppLanguage) =
        if (lang == AppLanguage.PL) "$score pkt" else "$score очков"

    fun randomMixTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "🎲 Losowy Super-Mix" else "🎲 Случайный Супер-Микс"

    fun randomMixDesc(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Mieszanka pytań ze wszystkich kategorii bez powtórzeń!" else "Ассорти вопросов из всех тем без повторов!"

    fun setupTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "USTAWIENIA ROZGRYWKI" else "НАСТРОЙКИ ИГРЫ"

    fun setupSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Dostosuj parametry przed dołączeniem graczy" else "Выберите параметры перед входом игроков"

    fun langStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "1. JĘZYK GRY" else "1. ЯЗЫК ИГРЫ"

    fun modeStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "2. DLA KOGO GRAMY?" else "2. ДЛЯ КОГО ИГРАЕМ?"

    fun formatStepLabel(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "3. FORMAT GRY" else "3. ФОРМАТ ИГРЫ"

    fun btnProceedToLobby(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "OTWÓRZ POKÓJ DLA GRACZY →" else "ОТКРЫТЬ КОМНАТУ ДЛЯ ИГРОКОВ →"

    fun btnBackToSetup(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "⚙️ Ustawienia [Wstecz]" else "⚙️ Настройки [Назад]"

    fun btnChooseTheme(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBIERZ TEMAT QUIZU →" else "ВЫБРАТЬ ТЕМУ КВИЗА →"

    fun themeSelectTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBIERZ TEMAT QUIZU" else "ВЫБЕРИТЕ ТЕМУ КВИЗА"

    fun themeSelectSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Wybierz kategorię pilotem TV lub na smartfonie prowadzącego" else "Выберите тему пультом ТВ или на смартфоне ведущего"

    fun btnStartQuizNow(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "ROZPOCZNIJ QUIZ! 🚀" else "НАЧАТЬ КВИЗ! 🚀"

    fun btnBackToLobby(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "← POWRÓT DO GRACZY" else "← НАЗАД К ИГРОКАМ"

    fun selectedThemeBadge(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "WYBRANO ✓" else "ВЫБРАНО ✓"

    fun scanQrInstruction(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "📱 Skieruj aparat telefonu na kod QR" else "📱 Наведи камеру телефона на QR-код"

    fun gamePausedTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "⏸️ GRA WSTRZYMANA" else "⏸️ ИГРА НА ПАУЗЕ"

    fun gamePausedSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "Prowadzący wstrzymał rozgrywkę" else "Ведущий поставил раунд на паузу"

    fun musicGuessTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "🎵 Zgadnij melodię: co to za utwór / film?" else "🎵 Угадай мелодию: какой трек / фильм звучит?"

    fun rebusGuessTitle(lang: AppLanguage) =
        if (lang == AppLanguage.PL) "🧩 Rozszyfruj kultowy film!" else "🧩 Разгадай культовый фильм по ребусу!"
}
