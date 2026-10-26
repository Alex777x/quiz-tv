package com.clean.quiztv.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.data.qr.QrCodeGenerator
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.Player
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.domain.model.Team
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.BorderFocused
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.CardDark
import com.clean.quiztv.ui.theme.GoldMedal
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.NeonViolet
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun RoomLobbyScreen(
    state: GameState.Lobby,
    serverUrl: String,
    onBackToSetup: () -> Unit,
    onProceedToThemeSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val qrBitmap = remember(serverUrl) {
        QrCodeGenerator.generateQrBitmap(serverUrl, sizePx = 520)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar: Branding, Mode Badges & Settings Shortcut ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(NeonCyan, NeonViolet)))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "QUIZ TV",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (state.selectedMode == GameMode.KIDS) NeonAmber.copy(alpha = 0.2f)
                            else NeonCyan.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.modeBadge(state.selectedMode, lang),
                        color = if (state.selectedMode == GameMode.KIDS) NeonAmber else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Format Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x333B82F6))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.formatBadge(state.gameFormat, lang),
                        color = Color(0xFF60A5FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive Settings / Back Button
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .scale(if (isBackFocused) 1.05f else 1.0f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x331E293B))
                    .border(
                        width = if (isBackFocused) 2.dp else 1.dp,
                        color = if (isBackFocused) BorderFocused else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isBackFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isBackFocused = focusState.isFocused
                    }
                    .focusable()
                    .clickable {
                        TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        onBackToSetup()
                    }
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = Strings.btnBackToSetup(lang),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Main Area: Left (QR & URL) & Right (Connected Players / Teams) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Left Half: Connection Instructions & Big QR
            DoubleBezelCard(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(24.dp),
                innerShape = RoundedCornerShape(21.dp),
                outerPadding = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.scanQr(lang),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Strings.orOpenBrowser(lang, serverUrl),
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Large crisp QR code
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("QR...", color = Color.Black, fontSize = 14.sp)
                        }
                    }

                    // Clean high-contrast scan instruction banner (100% visible, no clipping!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x331E293B))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Strings.scanQrInstruction(lang),
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Right Half: Joined Players or Team Columns
            DoubleBezelCard(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(24.dp),
                innerShape = RoundedCornerShape(21.dp),
                outerPadding = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.playersInRoom(lang),
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (state.players.isEmpty()) Color(0x33475569)
                                    else NeonEmerald.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${state.players.size} / 20",
                                color = if (state.players.isEmpty()) TextMuted else NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.players.isEmpty()) {
                        // Empty Waiting Box
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "📱",
                                    fontSize = 44.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = Strings.waitingForPlayersPrompt(lang),
                                    color = TextMuted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (state.gameFormat == GameFormat.TEAMS) {
                        // Dynamic Teams Grid (Supports up to 20 teams)
                        val teamsToDisplay = state.teams.ifEmpty {
                            listOf(
                                Team("team_1", if (lang == AppLanguage.PL) "Drużyna 1" else "Команда 1", "#EF4444", "🔴"),
                                Team("team_2", if (lang == AppLanguage.PL) "Drużyna 2" else "Команда 2", "#3B82F6", "🔵")
                            )
                        }
                        val gridCols = if (teamsToDisplay.size <= 2) 2 else if (teamsToDisplay.size <= 4) 2 else 3

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridCols),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(teamsToDisplay, key = { it.id }) { team ->
                                val teamPlayers = state.players.filter { it.teamId == team.id }
                                DynamicLobbyTeamBox(
                                    team = team,
                                    players = teamPlayers,
                                    lang = lang
                                )
                            }
                        }
                    } else {
                        // Individual Solo Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.players, key = { it.id }) { player ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + scaleIn()
                                ) {
                                    PlayerChip(player = player, lang = lang)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Bottom Bar: Choose Theme Action Button ---
        val canProceed = state.players.isNotEmpty()
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        var isProceedFocused by remember { mutableStateOf(false) }
        val proceedScale by animateFloatAsState(
            targetValue = if (isProceedFocused) 1.05f else 1.0f,
            animationSpec = tween(durationMillis = 200),
            label = "btnProceedThemeScale"
        )

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .scale(proceedScale)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (canProceed) Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF0284C7)))
                    else Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                )
                .border(
                    width = if (isProceedFocused) 3.dp else 1.dp,
                    color = if (isProceedFocused) BorderFocused else BorderLight,
                    shape = RoundedCornerShape(20.dp)
                )
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isProceedFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                    isProceedFocused = focusState.isFocused
                }
                .focusable()
                .clickable(enabled = canProceed) {
                    TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
                    onProceedToThemeSelect()
                }
                .padding(vertical = 15.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (canProceed) Strings.btnChooseTheme(lang) else Strings.waitingForPlayersBtn(lang),
                    color = if (canProceed) Color.White else TextMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                if (canProceed) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "🎮 [OK]", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DynamicLobbyTeamBox(
    team: Team,
    players: List<Player>,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(team.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(team.colorHex))
        } catch (_: Exception) {
            Color(0xFF3B82F6)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x220F172A))
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = team.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = team.name,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${players.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == AppLanguage.PL) "Brak graczy" else "Нет игроков",
                        color = TextMuted.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    players.forEach { p ->
                        PlayerChip(player = p, lang = lang)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(player: Player, lang: AppLanguage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.avatar, fontSize = 19.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
