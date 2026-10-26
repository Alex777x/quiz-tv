/**
 * Quiz TV Mobile Web Controller
 * 100% Offline Vanilla JS (WebSockets, Web Audio Synth, Dual-Language PL/RU)
 */

(function () {
    'use strict';

    // --- State & Constants ---
    const STORAGE_KEY_ID = 'quiztv_player_id';
    const STORAGE_KEY_NAME = 'quiztv_player_name';
    const STORAGE_KEY_AVATAR = 'quiztv_player_avatar';
    const STORAGE_KEY_LANG = 'quiztv_language';

    const AVATARS = ['🦊', '🐻', '🐼', '🐱', '🤖', '👽', '🏴‍☠️', '🥷', '🦁', '🦄', '🚀', '⭐'];

    let socket = null;
    let reconnectTimer = null;
    let isConnected = false;

    let currentLang = localStorage.getItem(STORAGE_KEY_LANG) || 'RU'; // 'RU' | 'PL'
    let myPlayerId = localStorage.getItem(STORAGE_KEY_ID) || '';
    let myName = localStorage.getItem(STORAGE_KEY_NAME) || '';
    let myAvatar = localStorage.getItem(STORAGE_KEY_AVATAR) || '🦊';

    let selectedFormat = 'INDIVIDUAL';
    let myTeamId = '';
    let currentTeams = [];
    let currentQuestionId = '';
    let questionStartTime = 0;
    let questionDurationSec = 20;
    let timerInterval = null;

    // --- Bilingual Dictionary ---
    const I18N = {
        RU: {
            langFlag: '🇷🇺',
            langLabel: 'РУССКИЙ',
            nextLang: 'PL',
            joinTitle: 'Вход в игру',
            joinSubtitle: 'Введи имя и выбери персонажа',
            namePlaceholder: 'Твое имя...',
            btnJoin: 'Войти в игру',
            waitingTitle: 'Ты в игре!',
            waitingSubtitle: 'Смотри на экран телевизора.<br>Игра скоро начнется!',
            lobbyCounterLabel: 'Игроков в комнате:',
            lockTitle: 'Ответ принят!',
            lockSubtitle: 'Смотри на телевизор...',
            revealCorrect: 'Верно!',
            revealIncorrect: 'Мимо!',
            streakLabel: '🔥 Стрик: ',
            standingRank: 'Твое место: ',
            standingScore: 'Всего очков: ',
            factTitle: '💡 Интересный факт',
            leaderboardTitle: 'Таблица лидеров',
            intermissionHint: 'Следующий вопрос начнется через несколько секунд...',
            podiumTitle: 'Игра окончена!',
            podiumScoreTitle: 'Твой финальный результат:',
            winnerPrefix: 'Победитель: ',
            btnPlayAgain: 'Сыграть еще раз',
            toastConnecting: 'Подключение к ТВ...',
            questionRound: (q, total) => `Вопрос ${q} из ${total}`,
            leaveBtn: 'Выйти',
            leaveConfirm: 'Вы действительно хотите покинуть игру?',
            formatLabel: 'Формат игры:',
            formatSolo: '👤 Соло',
            formatTeams: '👥 Команды',
            teamPickerTitle: 'Выбор команды (до 20):',
            createTeamBtn: '+ Создать',
            teamNamePlaceholder: 'Имя новой команды...',
            emptyTeamsHint: 'Пока нет команд. Введи название и создай первую!'
        },
        PL: {
            langFlag: '🇵🇱',
            langLabel: 'POLSKI',
            nextLang: 'RU',
            joinTitle: 'Dołącz do gry',
            joinSubtitle: 'Wpisz imię i wybierz postać',
            namePlaceholder: 'Twoje imię...',
            btnJoin: 'Wejdź do gry',
            waitingTitle: 'Jesteś w grze!',
            waitingSubtitle: 'Spójrz na ekran telewizora.<br>Gra wkrótce się rozpocznie!',
            lobbyCounterLabel: 'Graczy w pokoju:',
            lockTitle: 'Odpowiedź zapisana!',
            lockSubtitle: 'Patrz na telewizor...',
            revealCorrect: 'Brawo! Dobrze!',
            revealIncorrect: 'Niestety pudło!',
            streakLabel: '🔥 Seria: ',
            standingRank: 'Twoje miejsce: ',
            standingScore: 'Punkty: ',
            factTitle: '💡 Ciekawostka',
            leaderboardTitle: 'Tabela liderów',
            intermissionHint: 'Kolejne pytanie rozpocznie się za chwilę...',
            podiumTitle: 'Koniec gry!',
            podiumScoreTitle: 'Twój wynik końcowy:',
            winnerPrefix: 'Zwycięzca: ',
            btnPlayAgain: 'Zagraj ponownie',
            toastConnecting: 'Łączenie z TV...',
            questionRound: (q, total) => `Pytanie ${q} z ${total}`,
            leaveBtn: 'Wyjdź',
            leaveConfirm: 'Czy na pewno chcesz wyjść z gry?',
            formatLabel: 'Format gry:',
            formatSolo: '👤 Solo',
            formatTeams: '👥 Drużyny',
            teamPickerTitle: 'Wybór drużyny (do 20):',
            createTeamBtn: '+ Utwórz',
            teamNamePlaceholder: 'Nazwa nowej drużyny...',
            emptyTeamsHint: 'Brak drużyn. Wpisz nazwę i utwórz pierwszą!'
        }
    };

    // --- DOM Elements ---
    const toast = document.getElementById('connection-toast');
    const toastText = document.getElementById('toast-text');
    const btnLangToggle = document.getElementById('btn-lang-toggle');
    const langFlag = document.getElementById('lang-flag');
    const langLabel = document.getElementById('lang-label');
    const btnLeaveRoom = document.getElementById('btn-leave-room');
    const txtLeaveBtn = document.getElementById('txt-leave-btn');

    const screens = {
        join: document.getElementById('screen-join'),
        lobby: document.getElementById('screen-lobby'),
        question: document.getElementById('screen-question'),
        reveal: document.getElementById('screen-reveal'),
        leaderboard: document.getElementById('screen-leaderboard'),
        podium: document.getElementById('screen-podium')
    };

    // Join screen elements
    const avatarDisplay = document.getElementById('current-avatar');
    const avatarGrid = document.getElementById('avatar-grid');
    const nameInput = document.getElementById('player-name-input');
    const btnJoin = document.getElementById('btn-join');

    // Lobby screen elements
    const lobbyAvatar = document.getElementById('lobby-player-avatar');
    const lobbyName = document.getElementById('lobby-player-name');
    const teamSelectorBox = document.getElementById('team-selector-box');
    const txtTeamPickerTitle = document.getElementById('txt-team-picker-title');
    const newTeamInput = document.getElementById('new-team-name-input');
    const btnCreateTeam = document.getElementById('btn-create-team');
    const txtBtnCreateTeam = document.getElementById('txt-btn-create-team');
    const teamsListEl = document.getElementById('teams-list');
    const lobbyCount = document.getElementById('lobby-players-count');

    // Question screen elements
    const qRoundLabel = document.getElementById('q-round-label');
    const timerBar = document.getElementById('timer-bar');
    const qTextBox = document.getElementById('question-text-mobile');
    const answerButtons = document.querySelectorAll('.answer-btn');
    const lockOverlay = document.getElementById('answer-lock-overlay');

    // Reveal screen elements
    const revealCard = document.getElementById('reveal-card');
    const revealIcon = document.getElementById('reveal-icon');
    const revealTitle = document.getElementById('reveal-title');
    const revealPoints = document.getElementById('reveal-points');
    const revealStreakBox = document.getElementById('reveal-streak-box');
    const revealStreakVal = document.getElementById('reveal-streak-val');
    const revealRank = document.getElementById('reveal-rank');
    const revealTotalScore = document.getElementById('reveal-total-score');
    const revealFactBox = document.getElementById('reveal-fact-box');
    const revealFactText = document.getElementById('reveal-fact-text');

    // Leaderboard screen elements
    const leaderboardList = document.getElementById('leaderboard-list');

    // Podium screen elements
    const podiumRankBadge = document.getElementById('podium-rank-badge');
    const podiumFinalScore = document.getElementById('podium-final-score');
    const podiumWinnerText = document.getElementById('podium-winner-text');
    const btnPlayAgain = document.getElementById('btn-play-again');

    // --- Audio Synthesizer (Zero External Audio Files) ---
    const Audio = {
        ctx: null,

        init() {
            if (!this.ctx) {
                const AudioContext = window.AudioContext || window.webkitAudioContext;
                if (AudioContext) {
                    this.ctx = new AudioContext();
                }
            }
            if (this.ctx && this.ctx.state === 'suspended') {
                this.ctx.resume();
            }
        },

        playTone(freq, type, duration, gain = 0.15) {
            this.init();
            if (!this.ctx) return;
            try {
                const osc = this.ctx.createOscillator();
                const g = this.ctx.createGain();
                osc.type = type;
                osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
                g.gain.setValueAtTime(gain, this.ctx.currentTime);
                g.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
                osc.connect(g);
                g.connect(this.ctx.destination);
                osc.start();
                osc.stop(this.ctx.currentTime + duration);
            } catch (_) {}
        },

        tap() {
            this.playTone(680, 'sine', 0.08, 0.2);
            if (navigator.vibrate) navigator.vibrate(25);
        },

        tick() {
            this.playTone(1100, 'sine', 0.04, 0.12);
        },

        correct() {
            this.init();
            if (!this.ctx) return;
            const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
            notes.forEach((freq, idx) => {
                setTimeout(() => {
                    this.playTone(freq, 'triangle', 0.28, 0.22);
                }, idx * 75);
            });
            if (navigator.vibrate) navigator.vibrate([60, 40, 100]);
        },

        wrong() {
            this.init();
            if (!this.ctx) return;
            this.playTone(280, 'sawtooth', 0.18, 0.25);
            setTimeout(() => this.playTone(210, 'sawtooth', 0.32, 0.25), 140);
            if (navigator.vibrate) navigator.vibrate(220);
        }
    };

    // --- UI Localization Applier ---
    function applyLanguage(lang) {
        currentLang = lang;
        localStorage.setItem(STORAGE_KEY_LANG, lang);
        const t = I18N[lang] || I18N.RU;

        langFlag.textContent = t.langFlag;
        langLabel.textContent = t.langLabel;

        const setTxt = (id, text) => {
            const el = document.getElementById(id);
            if (el) el.innerHTML = text;
        };

        setTxt('txt-join-title', t.joinTitle);
        setTxt('txt-join-subtitle', t.joinSubtitle);
        if (nameInput) nameInput.placeholder = t.namePlaceholder;
        setTxt('txt-btn-join', t.btnJoin);

        setTxt('txt-waiting-title', t.waitingTitle);
        setTxt('txt-waiting-subtitle', t.waitingSubtitle);
        setTxt('txt-lobby-counter-label', t.lobbyCounterLabel);

        setTxt('txt-lock-title', t.lockTitle);
        setTxt('txt-lock-subtitle', t.lockSubtitle);

        setTxt('txt-reveal-streak-label', t.streakLabel);
        setTxt('txt-reveal-fact-title', t.factTitle);

        setTxt('txt-leaderboard-title', t.leaderboardTitle);
        setTxt('txt-intermission-hint', t.intermissionHint);

        setTxt('txt-podium-title', t.podiumTitle);
        setTxt('txt-podium-score-title', t.podiumScoreTitle);
        setTxt('txt-btn-play-again', t.btnPlayAgain);

        if (txtLeaveBtn) txtLeaveBtn.textContent = t.leaveBtn;
        if (txtTeamPickerTitle) txtTeamPickerTitle.textContent = t.teamPickerTitle;
        if (txtBtnCreateTeam) txtBtnCreateTeam.textContent = t.createTeamBtn;
        if (newTeamInput) newTeamInput.placeholder = t.teamNamePlaceholder;

        renderTeamsList();
    }

    function updateFormatView() {
        if (selectedFormat === 'TEAMS') {
            if (teamSelectorBox) teamSelectorBox.classList.remove('hidden');
            renderTeamsList();
        } else {
            if (teamSelectorBox) teamSelectorBox.classList.add('hidden');
        }
    }

    function renderTeamsList() {
        if (!teamsListEl) return;
        teamsListEl.innerHTML = '';
        const t = I18N[currentLang] || I18N.RU;

        if (!currentTeams || currentTeams.length === 0) {
            const emptyP = document.createElement('div');
            emptyP.className = 'empty-teams-hint';
            emptyP.style.cssText = 'color: var(--text-muted); font-size: 12.5px; text-align: center; padding: 12px;';
            emptyP.textContent = t.emptyTeamsHint;
            teamsListEl.appendChild(emptyP);
            return;
        }

        currentTeams.forEach(team => {
            const item = document.createElement('div');
            item.className = `team-list-item ${team.id === myTeamId ? 'selected' : ''}`;
            item.style.borderColor = team.colorHex;

            const left = document.createElement('div');
            left.className = 'team-item-left';

            const emoji = document.createElement('span');
            emoji.className = 'team-item-emoji';
            emoji.textContent = team.emoji;

            const name = document.createElement('span');
            name.className = 'team-item-name';
            name.textContent = team.name;

            left.appendChild(emoji);
            left.appendChild(name);

            const badge = document.createElement('span');
            badge.className = 'team-item-badge';
            const count = team.memberCount || 0;
            badge.textContent = (currentLang === 'PL') ? `${count} os.` : `${count} игр.`;

            item.appendChild(left);
            item.appendChild(badge);

            item.onclick = () => {
                Audio.tap();
                myTeamId = team.id;
                renderTeamsList();
                send({
                    type: 'SELECT_TEAM',
                    teamId: team.id
                });
            };

            teamsListEl.appendChild(item);
        });
    }

    // --- UI Navigation ---
    function showScreen(name) {
        Object.keys(screens).forEach(key => {
            if (key === name) {
                screens[key].classList.add('active');
            } else {
                screens[key].classList.remove('active');
            }
        });

        if (btnLeaveRoom) {
            if (name === 'join') {
                btnLeaveRoom.classList.add('hidden');
            } else {
                btnLeaveRoom.classList.remove('hidden');
            }
        }
    }

    function showToast(text) {
        toastText.textContent = text;
        toast.classList.remove('hidden');
    }

    function hideToast() {
        toast.classList.add('hidden');
    }

    // --- WebSocket Networking ---
    function connectSocket() {
        if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
            return;
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const host = window.location.host || '127.0.0.1:8080';
        const wsUrl = `${protocol}//${host}/ws/game`;

        const t = I18N[currentLang] || I18N.RU;
        showToast(t.toastConnecting);

        try {
            socket = new WebSocket(wsUrl);
        } catch (e) {
            scheduleReconnect();
            return;
        }

        socket.onopen = function () {
            isConnected = true;
            hideToast();
            clearTimeout(reconnectTimer);

            if (myPlayerId) {
                send({
                    type: 'RECONNECT',
                    playerId: myPlayerId
                });
            }
        };

        socket.onmessage = function (event) {
            try {
                const msg = JSON.parse(event.data);
                handleServerMessage(msg);
            } catch (err) {
                console.error('Failed to parse WebSocket message', err);
            }
        };

        socket.onerror = function () {
            isConnected = false;
        };

        socket.onclose = function () {
            isConnected = false;
            scheduleReconnect();
        };
    }

    function scheduleReconnect() {
        clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(connectSocket, 2000);
    }

    function send(msgObj) {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify(msgObj));
        }
    }

    // --- Server Message Dispatcher ---
    function handleServerMessage(msg) {
        const t = I18N[currentLang] || I18N.RU;

        switch (msg.type) {
            case 'WELCOME':
                myPlayerId = msg.playerId;
                localStorage.setItem(STORAGE_KEY_ID, myPlayerId);

                if (msg.language && msg.language !== currentLang) {
                    applyLanguage(msg.language);
                }
                if (msg.gameFormat) {
                    selectedFormat = msg.gameFormat;
                    updateFormatView();
                }
                if (msg.teamId) {
                    myTeamId = msg.teamId;
                }
                if (msg.teams) {
                    currentTeams = msg.teams;
                    renderTeamsList();
                }

                lobbyName.textContent = myName || 'Игрок';
                lobbyAvatar.textContent = myAvatar;

                if (msg.serverState === 'LOBBY' || !msg.serverState) {
                    showScreen('lobby');
                }
                break;

            case 'LOBBY_UPDATE':
                lobbyCount.textContent = msg.playersCount || '1';

                if (msg.language && msg.language !== currentLang) {
                    applyLanguage(msg.language);
                }

                if (msg.gameFormat && msg.gameFormat !== selectedFormat) {
                    selectedFormat = msg.gameFormat;
                    updateFormatView();
                }

                if (msg.teams) {
                    currentTeams = msg.teams;
                    renderTeamsList();
                }

                if (msg.players) {
                    const me = msg.players.find(p => p.id === myPlayerId);
                    if (me && me.teamId) {
                        myTeamId = me.teamId;
                        renderTeamsList();
                    }
                }
                break;

            case 'QUESTION_START':
                currentQuestionId = msg.questionId || `q_${msg.questionIndex}`;
                questionStartTime = performance.now();
                questionDurationSec = msg.timeLimitSeconds || 20;

                qRoundLabel.textContent = t.questionRound(msg.questionIndex, msg.totalQuestions);

                if (msg.gameMode === 'KIDS' && msg.text) {
                    qTextBox.textContent = msg.text;
                    qTextBox.classList.remove('hidden');
                } else {
                    qTextBox.classList.add('hidden');
                }

                // Populate button labels
                if (msg.options && msg.options.length) {
                    msg.options.forEach((optText, i) => {
                        const lbl = document.getElementById(`opt-label-${i}`);
                        if (lbl) lbl.textContent = optText;
                    });
                }

                // Reset answer buttons
                answerButtons.forEach(btn => {
                    btn.disabled = false;
                    btn.classList.remove('selected', 'dimmed');
                });
                lockOverlay.classList.add('hidden');

                showScreen('question');
                startMobileTimer(questionDurationSec);
                break;

            case 'ANSWER_ACK':
                // Visual confirmation that answer was received
                Audio.tap();
                lockOverlay.classList.remove('hidden');
                answerButtons.forEach((btn, idx) => {
                    btn.disabled = true;
                    if (idx === msg.selectedIndex) {
                        btn.classList.add('selected');
                    } else {
                        btn.classList.add('dimmed');
                    }
                });
                break;

            case 'ROUND_RESULT':
                clearInterval(timerInterval);

                if (msg.isCorrect) {
                    Audio.correct();
                    revealCard.className = 'reveal-banner correct double-bezel';
                    revealIcon.textContent = '🎉';
                    revealTitle.textContent = t.revealCorrect;
                    revealPoints.textContent = `+${msg.pointsEarned}`;
                    revealPoints.style.display = 'inline-block';
                } else {
                    Audio.wrong();
                    revealCard.className = 'reveal-banner incorrect double-bezel';
                    revealIcon.textContent = '❌';
                    revealTitle.textContent = t.revealIncorrect;
                    revealPoints.style.display = 'none';
                }

                if (msg.streak > 1) {
                    revealStreakVal.textContent = msg.streak;
                    revealStreakBox.classList.remove('hidden');
                } else {
                    revealStreakBox.classList.add('hidden');
                }

                revealRank.textContent = `#${msg.rank}`;
                revealTotalScore.textContent = msg.totalScore;

                if (msg.funFact) {
                    revealFactText.textContent = msg.funFact;
                    revealFactBox.classList.remove('hidden');
                } else {
                    revealFactBox.classList.add('hidden');
                }

                showScreen('reveal');
                break;

            case 'LEADERBOARD_UPDATE':
                renderLeaderboard(msg.standings || []);
                showScreen('leaderboard');
                break;

            case 'GAME_OVER':
                podiumFinalScore.textContent = msg.finalScore;
                podiumWinnerText.textContent = `${t.winnerPrefix}${msg.winnerName}`;

                const medals = ['🥇', '🥈', '🥉'];
                podiumRankBadge.textContent = medals[msg.finalRank - 1] || '🏅';

                showScreen('podium');
                break;

            case 'SYNC_STATE':
                handleSyncState(msg);
                break;

            case 'ERROR':
                showToast(msg.message);
                if (msg.message.indexOf('Сессия не найдена') !== -1 ||
                    msg.message.indexOf('Sesja wygasła') !== -1 ||
                    msg.message.indexOf('не найдена') !== -1) {
                    localStorage.removeItem(STORAGE_KEY_ID);
                    myPlayerId = '';
                    myTeamId = '';
                    setTimeout(() => {
                        hideToast();
                        showScreen('join');
                    }, 1200);
                }
                break;
        }
    }

    function handleSyncState(msg) {
        const t = I18N[currentLang] || I18N.RU;
        switch (msg.state) {
            case 'LOBBY':
                showScreen('lobby');
                break;
            case 'QUESTION':
                showScreen('question');
                currentQuestionId = msg.questionId || ('q_' + msg.questionIndex);
                questionDurationSec = msg.timeLimitSeconds || questionDurationSec;
                if (msg.timeRemainingSeconds > 0) {
                    questionStartTime = performance.now() - ((questionDurationSec - msg.timeRemainingSeconds) * 1000);
                } else {
                    questionStartTime = performance.now();
                }

                if (msg.questionIndex && msg.totalQuestions) {
                    qRoundLabel.textContent = t.questionRound(msg.questionIndex, msg.totalQuestions);
                }
                if (msg.gameMode === 'KIDS' && msg.questionText) {
                    qTextBox.textContent = msg.questionText;
                    qTextBox.classList.remove('hidden');
                } else {
                    qTextBox.classList.add('hidden');
                }

                if (msg.options && msg.options.length) {
                    msg.options.forEach((optText, i) => {
                        const lbl = document.getElementById(`opt-label-${i}`);
                        if (lbl) lbl.textContent = optText;
                    });
                }
                if (msg.alreadyAnswered) {
                    lockOverlay.classList.remove('hidden');
                    answerButtons.forEach((b, idx) => {
                        b.disabled = true;
                        if (typeof msg.selectedIndex === 'number' && idx === msg.selectedIndex) {
                            b.classList.add('selected');
                            b.classList.remove('dimmed');
                        } else if (typeof msg.selectedIndex === 'number') {
                            b.classList.add('dimmed');
                            b.classList.remove('selected');
                        }
                    });
                } else {
                    lockOverlay.classList.add('hidden');
                    answerButtons.forEach(b => {
                        b.disabled = false;
                        b.classList.remove('selected', 'dimmed');
                    });
                }
                if (msg.timeRemainingSeconds > 0) {
                    startMobileTimer(msg.timeRemainingSeconds);
                }
                break;
            case 'REVEAL':
                showScreen('reveal');
                break;
            case 'LEADERBOARD':
                showScreen('leaderboard');
                break;
            case 'PODIUM':
                showScreen('podium');
                break;
        }
    }

    function startMobileTimer(durationSec) {
        clearInterval(timerInterval);
        timerBar.style.transform = 'scaleX(1)';
        const start = performance.now();
        const totalMs = durationSec * 1000;

        let lastSecBeeped = -1;

        timerInterval = setInterval(() => {
            const elapsed = Math.max(0, performance.now() - start);
            const remainingRatio = Math.max(0, 1 - (elapsed / totalMs));
            timerBar.style.transform = `scaleX(${remainingRatio})`;

            const remainingSec = Math.ceil((totalMs - elapsed) / 1000);
            if (remainingSec <= 5 && remainingSec > 0 && remainingSec !== lastSecBeeped) {
                lastSecBeeped = remainingSec;
                Audio.tick();
            }

            if (remainingRatio <= 0) {
                clearInterval(timerInterval);
            }
        }, 100);
    }

    function renderLeaderboard(standings) {
        leaderboardList.innerHTML = '';
        standings.forEach(st => {
            const row = document.createElement('div');
            row.className = `leader-item ${st.playerId === myPlayerId ? 'is-me' : ''}`;

            const rankSpan = document.createElement('span');
            rankSpan.className = `leader-rank top${st.rank}`;
            rankSpan.textContent = `#${st.rank}`;

            const avatarSpan = document.createElement('span');
            avatarSpan.className = 'leader-avatar';
            avatarSpan.textContent = st.avatar;

            const nameSpan = document.createElement('span');
            nameSpan.className = 'leader-name';
            nameSpan.textContent = st.name;

            const scoreSpan = document.createElement('span');
            scoreSpan.className = 'leader-score';
            scoreSpan.textContent = st.score;

            row.appendChild(rankSpan);
            row.appendChild(avatarSpan);
            row.appendChild(nameSpan);
            row.appendChild(scoreSpan);
            leaderboardList.appendChild(row);
        });
    }

    // --- DOM Event Listeners ---
    function initUI() {
        // Apply default language
        applyLanguage(currentLang);

        // Language toggle button handler
        btnLangToggle.onclick = () => {
            Audio.tap();
            const nextLang = (currentLang === 'RU') ? 'PL' : 'RU';
            applyLanguage(nextLang);
            send({
                type: 'SET_LANGUAGE',
                language: nextLang
            });
        };

        // Render avatars
        avatarGrid.innerHTML = '';
        AVATARS.forEach(av => {
            const b = document.createElement('button');
            b.className = `avatar-btn ${av === myAvatar ? 'selected' : ''}`;
            b.textContent = av;
            b.onclick = () => {
                Audio.tap();
                myAvatar = av;
                avatarDisplay.textContent = av;
                avatarDisplay.classList.add('pop');
                setTimeout(() => avatarDisplay.classList.remove('pop'), 200);

                avatarGrid.querySelectorAll('.avatar-btn').forEach(btn => btn.classList.remove('selected'));
                b.classList.add('selected');
            };
            avatarGrid.appendChild(b);
        });

        avatarDisplay.textContent = myAvatar;
        if (myName) {
            nameInput.value = myName;
        }

        // Join button
        btnJoin.onclick = () => {
            Audio.init();
            Audio.tap();
            const name = nameInput.value.trim();
            if (!name) {
                nameInput.focus();
                return;
            }

            myName = name;
            localStorage.setItem(STORAGE_KEY_NAME, myName);
            localStorage.setItem(STORAGE_KEY_AVATAR, myAvatar);

            send({
                type: 'JOIN',
                playerId: myPlayerId || null,
                name: myName,
                avatar: myAvatar
            });
        };

        nameInput.onkeydown = (e) => {
            if (e.key === 'Enter') {
                btnJoin.click();
            }
        };

        // Create Team
        if (btnCreateTeam && newTeamInput) {
            btnCreateTeam.onclick = () => {
                Audio.tap();
                const name = newTeamInput.value.trim();
                if (!name) {
                    newTeamInput.focus();
                    return;
                }
                send({
                    type: 'CREATE_TEAM',
                    name: name
                });
                newTeamInput.value = '';
            };

            newTeamInput.onkeydown = (e) => {
                if (e.key === 'Enter') {
                    btnCreateTeam.click();
                }
            };
        }

        // Leave room button
        if (btnLeaveRoom) {
            btnLeaveRoom.onclick = () => {
                Audio.tap();
                const t = I18N[currentLang] || I18N.RU;
                if (confirm(t.leaveConfirm)) {
                    send({ type: 'LEAVE_ROOM' });
                    localStorage.removeItem(STORAGE_KEY_ID);
                    myPlayerId = '';
                    myTeamId = null;
                    if (socket) {
                        socket.close();
                    }
                    showScreen('join');
                }
            };
        }

        // Answer Buttons (0: Red, 1: Blue, 2: Yellow, 3: Green)
        answerButtons.forEach(btn => {
            btn.onclick = () => {
                const idx = parseInt(btn.getAttribute('data-index'), 10);
                const elapsedMs = Math.max(0, Math.round(performance.now() - questionStartTime));

                Audio.tap();
                lockOverlay.classList.remove('hidden');
                answerButtons.forEach((b, bIdx) => {
                    b.disabled = true;
                    if (bIdx === idx) {
                        b.classList.add('selected');
                        b.classList.remove('dimmed');
                    } else {
                        b.classList.add('dimmed');
                        b.classList.remove('selected');
                    }
                });

                send({
                    type: 'SUBMIT_ANSWER',
                    questionId: currentQuestionId,
                    optionIndex: idx,
                    timeElapsedMs: elapsedMs
                });
            };
        });

        // Play Again
        if (btnPlayAgain) {
            btnPlayAgain.onclick = () => {
                Audio.tap();
                send({
                    type: 'PLAY_AGAIN'
                });
            };
        }
    }

    // --- Bootstrap ---
    window.addEventListener('DOMContentLoaded', () => {
        initUI();
        connectSocket();
    });
})();
