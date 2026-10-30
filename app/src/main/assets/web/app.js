/**
 * Quiz TV Studio Mobile Controller
 * 100% Offline Vanilla JS • Zero Emoji Clutter • Bilingual RU/PL
 */

(function () {
    'use strict';

    const STORAGE_KEY_ID = 'quiztv_player_id';
    const STORAGE_KEY_NAME = 'quiztv_player_name';
    const STORAGE_KEY_AVATAR = 'quiztv_player_avatar';
    const STORAGE_KEY_LANG = 'quiztv_language';

    // Clean studio callsigns (zero cartoon emojis)
    const AVATARS = ['01', '02', '03', '04', '05', '06', '07', '08'];

    let socket = null;
    let reconnectTimer = null;
    let isConnected = false;

    let currentLang = localStorage.getItem(STORAGE_KEY_LANG) || 'RU';
    let myPlayerId = localStorage.getItem(STORAGE_KEY_ID) || '';
    let myName = localStorage.getItem(STORAGE_KEY_NAME) || '';
    let savedAv = localStorage.getItem(STORAGE_KEY_AVATAR) || '01';
    let myAvatar = AVATARS.includes(savedAv) ? savedAv : '01';

    let selectedFormat = 'INDIVIDUAL';
    let myTeamId = '';
    let currentTeams = [];
    let currentQuestionId = '';
    let questionStartTime = 0;
    let questionDurationSec = 20;
    let timerInterval = null;

    const I18N = {
        RU: {
            langLabel: 'RU',
            joinEyebrow: 'ПУЛЬТ УЧАСТНИКА',
            joinTitle: 'Вход в игру',
            joinSubtitle: 'Введите имя и выберите позывной',
            nameLabel: 'ВАШЕ ИМЯ',
            badgeLabel: 'НОМЕР ИГРОКА',
            namePlaceholder: 'Введите имя...',
            btnJoin: 'ПОДКЛЮЧИТЬСЯ',
            playerStatus: 'ПОДКЛЮЧЕН К СТУДИИ',
            waitingTitle: 'Вы в комнате',
            waitingSubtitle: 'Обратите внимание на экран телевизора.<br>Раунд начнется автоматически.',
            lobbyCounterLabel: 'Участников в комнате:',
            lockTitle: 'Ответ зафиксирован',
            lockSubtitle: 'Ожидайте завершения таймера на ТВ',
            revealCorrect: 'ВЕРНО',
            revealIncorrect: 'НЕВЕРНО',
            streakLabel: 'СЕРИЯ x',
            standingRank: 'МЕСТО',
            standingScore: 'СЧЕТ',
            factTitle: 'КОММЕНТАРИЙ К ОТВЕТУ',
            leaderboardTitle: 'Турнирная таблица',
            intermissionHint: 'Подготовка следующего вопроса...',
            podiumTitle: 'ИТОГИ ПАРТИИ',
            podiumScoreTitle: 'ФИНАЛЬНЫЙ СЧЕТ',
            winnerPrefix: 'Победитель: ',
            btnPlayAgain: 'НОВАЯ ИГРА',
            toastConnecting: 'Подключение к ТВ...',
            questionRound: (q, total) => `ВОПРОС ${q} / ${total}`,
            leaveBtn: 'ВЫХОД',
            leaveConfirm: 'Покинуть текущую комнату?',
            teamPickerTitle: 'ВЫБОР КОМАНДЫ',
            createTeamBtn: 'СОЗДАТЬ',
            teamNamePlaceholder: 'Название команды...',
            emptyTeamsHint: 'Команды еще не созданы. Введите название выше.'
        },
        PL: {
            langLabel: 'PL',
            joinEyebrow: 'KONTROLER GRACZA',
            joinTitle: 'Dołącz do gry',
            joinSubtitle: 'Wpisz imię i wybierz numer gracza',
            nameLabel: 'TWOJE IMIĘ',
            badgeLabel: 'NUMER GRACZA',
            namePlaceholder: 'Wpisz imię...',
            btnJoin: 'POŁĄCZ',
            playerStatus: 'POŁĄCZONO ZE STUDIEM',
            waitingTitle: 'Jesteś w pokoju',
            waitingSubtitle: 'Spójrz na ekran telewizora.<br>Runda rozpocznie się automatycznie.',
            lobbyCounterLabel: 'Graczy w pokoju:',
            lockTitle: 'Odpowiedź zapisana',
            lockSubtitle: 'Poczekaj na zakończenie odliczania na TV',
            revealCorrect: 'DOBRZE',
            revealIncorrect: 'BŁĄD',
            streakLabel: 'SERIA x',
            standingRank: 'MIEJSCE',
            standingScore: 'PUNKTY',
            factTitle: 'KOMENTARZ DO ODPOWIEDZI',
            leaderboardTitle: 'Klasyfikacja',
            intermissionHint: 'Przygotowanie kolejnego pytania...',
            podiumTitle: 'WYNIKI KOŃCOWE',
            podiumScoreTitle: 'WYNIK KOŃCOWY',
            winnerPrefix: 'Zwycięzca: ',
            btnPlayAgain: 'NOWA GRA',
            toastConnecting: 'Łączenie z TV...',
            questionRound: (q, total) => `PYTANIE ${q} / ${total}`,
            leaveBtn: 'WYJDŹ',
            leaveConfirm: 'Czy na pewno chcesz opuścić pokój?',
            teamPickerTitle: 'WYBÓR DRUŻYNY',
            createTeamBtn: 'UTWÓRZ',
            teamNamePlaceholder: 'Nazwa drużyny...',
            emptyTeamsHint: 'Brak drużyn. Wpisz nazwę powyżej.'
        }
    };

    // --- DOM Elements ---
    const toast = document.getElementById('connection-toast');
    const toastText = document.getElementById('toast-text');
    const btnLangToggle = document.getElementById('btn-lang-toggle');
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

    const avatarDisplay = document.getElementById('current-avatar');
    const avatarGrid = document.getElementById('avatar-grid');
    const nameInput = document.getElementById('player-name-input');
    const btnJoin = document.getElementById('btn-join');

    const lobbyAvatar = document.getElementById('lobby-player-avatar');
    const lobbyName = document.getElementById('lobby-player-name');
    const teamSelectorBox = document.getElementById('team-selector-box');
    const txtTeamPickerTitle = document.getElementById('txt-team-picker-title');
    const newTeamInput = document.getElementById('new-team-name-input');
    const btnCreateTeam = document.getElementById('btn-create-team');
    const txtBtnCreateTeam = document.getElementById('txt-btn-create-team');
    const teamsListEl = document.getElementById('teams-list');
    const lobbyCount = document.getElementById('lobby-players-count');

    const qRoundLabel = document.getElementById('q-round-label');
    const timerBar = document.getElementById('timer-bar');
    const qTextBox = document.getElementById('question-text-mobile');
    const answerButtons = document.querySelectorAll('.answer-btn');
    const lockOverlay = document.getElementById('answer-lock-overlay');

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

    const leaderboardList = document.getElementById('leaderboard-list');

    const podiumRankBadge = document.getElementById('podium-rank-badge');
    const podiumFinalScore = document.getElementById('podium-final-score');
    const podiumWinnerText = document.getElementById('podium-winner-text');
    const btnPlayAgain = document.getElementById('btn-play-again');

    // --- Audio Feedback ---
    const Audio = {
        ctx: null,
        init() {
            if (!this.ctx) {
                const AudioContext = window.AudioContext || window.webkitAudioContext;
                if (AudioContext) this.ctx = new AudioContext();
            }
            if (this.ctx && this.ctx.state === 'suspended') {
                this.ctx.resume();
            }
        },
        playTone(freq, type, duration, gain = 0.12) {
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
            this.playTone(620, 'sine', 0.06, 0.15);
            if (navigator.vibrate) navigator.vibrate(18);
        },
        tick() {
            this.playTone(980, 'sine', 0.035, 0.08);
        },
        correct() {
            this.init();
            if (!this.ctx) return;
            [523.25, 659.25, 783.99, 1046.50].forEach((freq, idx) => {
                setTimeout(() => this.playTone(freq, 'triangle', 0.22, 0.18), idx * 70);
            });
            if (navigator.vibrate) navigator.vibrate([45, 30, 80]);
        },
        wrong() {
            this.init();
            if (!this.ctx) return;
            this.playTone(250, 'sawtooth', 0.16, 0.18);
            setTimeout(() => this.playTone(195, 'sawtooth', 0.26, 0.18), 130);
            if (navigator.vibrate) navigator.vibrate(180);
        }
    };

    function applyLanguage(lang) {
        currentLang = lang;
        localStorage.setItem(STORAGE_KEY_LANG, lang);
        const t = I18N[lang] || I18N.RU;

        if (langLabel) langLabel.textContent = t.langLabel;

        const setTxt = (id, text) => {
            const el = document.getElementById(id);
            if (el) el.innerHTML = text;
        };

        setTxt('txt-join-eyebrow', t.joinEyebrow);
        setTxt('txt-join-title', t.joinTitle);
        setTxt('txt-join-subtitle', t.joinSubtitle);
        setTxt('txt-name-label', t.nameLabel);
        setTxt('txt-badge-label', t.badgeLabel);
        if (nameInput) nameInput.placeholder = t.namePlaceholder;
        setTxt('txt-btn-join', t.btnJoin);

        setTxt('txt-player-status', t.playerStatus);
        setTxt('txt-waiting-title', t.waitingTitle);
        setTxt('txt-waiting-subtitle', t.waitingSubtitle);
        setTxt('txt-lobby-counter-label', t.lobbyCounterLabel);

        setTxt('txt-lock-title', t.lockTitle);
        setTxt('txt-lock-subtitle', t.lockSubtitle);

        setTxt('txt-reveal-streak-label', t.streakLabel);
        setTxt('txt-reveal-standing-rank', t.standingRank);
        setTxt('txt-reveal-standing-score', t.standingScore);
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
            emptyP.style.cssText = 'color: var(--text-muted); font-size: 12.5px; text-align: center; padding: 12px;';
            emptyP.textContent = t.emptyTeamsHint;
            teamsListEl.appendChild(emptyP);
            return;
        }

        currentTeams.forEach(team => {
            const item = document.createElement('div');
            item.className = `team-list-item ${team.id === myTeamId ? 'selected' : ''}`;

            const left = document.createElement('div');
            left.className = 'team-item-left';

            const dot = document.createElement('span');
            dot.className = 'team-item-dot';
            dot.style.backgroundColor = team.colorHex || '#38BDF8';

            const name = document.createElement('span');
            name.className = 'team-item-name';
            name.textContent = team.name;

            left.appendChild(dot);
            left.appendChild(name);

            const badge = document.createElement('span');
            badge.className = 'team-item-badge';
            const count = team.memberCount || 0;
            badge.textContent = `${count}`;

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
        reconnectTimer = setTimeout(connectSocket, 1500);
    }

    function checkAndResync() {
        if (!socket || socket.readyState !== WebSocket.OPEN) {
            connectSocket();
        } else if (myPlayerId) {
            send({
                type: 'RECONNECT',
                playerId: myPlayerId
            });
        }
    }

    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') {
            checkAndResync();
        }
    });

    window.addEventListener('focus', () => {
        checkAndResync();
    });

    window.addEventListener('online', () => {
        checkAndResync();
    });

    setInterval(() => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            send({ type: 'PING' });
        } else if (myPlayerId && (!socket || socket.readyState === WebSocket.CLOSED)) {
            connectSocket();
        }
    }, 8000);

    function send(msgObj) {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify(msgObj));
        }
    }

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

                if (msg.text) {
                    qTextBox.textContent = msg.text;
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

                answerButtons.forEach(btn => {
                    btn.disabled = false;
                    btn.classList.remove('selected', 'dimmed');
                });
                lockOverlay.classList.add('hidden');

                showScreen('question');
                startMobileTimer(questionDurationSec);
                break;

            case 'ANSWER_ACK':
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
                    revealIcon.textContent = '✓';
                    revealTitle.textContent = t.revealCorrect;
                    revealPoints.textContent = `+${msg.pointsEarned}`;
                    revealPoints.style.display = 'inline-block';
                } else {
                    Audio.wrong();
                    revealCard.className = 'reveal-banner incorrect double-bezel';
                    revealIcon.textContent = '×';
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

                if (msg.funFact &&
                    msg.funFact.toLowerCase().indexOf('правильный ответ') === -1 &&
                    msg.funFact.toLowerCase().indexOf('prawidłowa odpowiedź') === -1) {
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
                podiumRankBadge.textContent = String(msg.finalRank || 1).padStart(2, '0');
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
                if (msg.questionText) {
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
            rankSpan.textContent = String(st.rank).padStart(2, '0');

            const avatarSpan = document.createElement('span');
            avatarSpan.className = 'leader-avatar';
            const avText = (st.avatar && st.avatar.length <= 2) ? st.avatar : (st.name || 'P').substring(0, 2).toUpperCase();
            avatarSpan.textContent = avText;

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

    function initUI() {
        applyLanguage(currentLang);

        btnLangToggle.onclick = () => {
            Audio.tap();
            const nextLang = (currentLang === 'RU') ? 'PL' : 'RU';
            applyLanguage(nextLang);
            send({
                type: 'SET_LANGUAGE',
                language: nextLang
            });
        };

        avatarGrid.innerHTML = '';
        AVATARS.forEach(av => {
            const b = document.createElement('button');
            b.className = `avatar-btn ${av === myAvatar ? 'selected' : ''}`;
            b.textContent = `#${av}`;
            b.onclick = () => {
                Audio.tap();
                myAvatar = av;
                if (avatarDisplay) avatarDisplay.textContent = av;
                avatarGrid.querySelectorAll('.avatar-btn').forEach(btn => btn.classList.remove('selected'));
                b.classList.add('selected');
            };
            avatarGrid.appendChild(b);
        });

        if (avatarDisplay) avatarDisplay.textContent = myAvatar;
        if (myName) {
            nameInput.value = myName;
        }

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

        if (btnPlayAgain) {
            btnPlayAgain.onclick = () => {
                Audio.tap();
                send({
                    type: 'PLAY_AGAIN'
                });
            };
        }
    }

    window.addEventListener('DOMContentLoaded', () => {
        initUI();
        connectSocket();
    });
})();
