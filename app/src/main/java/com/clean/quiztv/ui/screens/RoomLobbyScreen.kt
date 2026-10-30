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
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
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
        // --- Top Bar: Clean Architectural Badges & Back Action ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1F38BDF8))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "QUIZ TV",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mode Badge
                val modeColor = if (state.selectedMode == GameMode.KIDS) NeonAmber else NeonCyan
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(modeColor.copy(alpha = 0.14f))
                        .border(1.dp, modeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.modeBadge(state.selectedMode, lang),
                        color = modeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Format Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1FFFFFFF))
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.formatBadge(state.gameFormat, lang),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Back to Setup Button
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .scale(if (isBackFocused) 1.04f else 1.0f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161B26))
                    .border(
                        width = if (isBackFocused) 2.dp else 1.dp,
                        color = if (isBackFocused) BorderFocused else BorderLight,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isBackFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isBackFocused = focusState.isFocused
                    }
                    .clickable {
                        TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        onBackToSetup()
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.btnBackToSetup(lang),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // --- Main Area: Left (QR & URL) & Right (Connected Players / Teams) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Half: Connection Instructions & Big QR
            DoubleBezelCard(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(18.dp),
                innerShape = RoundedCornerShape(16.dp),
                outerPadding = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.scanQr(lang),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Strings.orOpenBrowser(lang, serverUrl),
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Large crisp QR code
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(RoundedCornerShape(14.dp))
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
                            Text("QR", color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Right Half: Joined Players or Team Columns
            DoubleBezelCard(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(18.dp),
                innerShape = RoundedCornerShape(16.dp),
                outerPadding = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.playersInRoom(lang),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (state.players.isEmpty()) Color(0x1FFFFFFF)
                                    else NeonEmerald.copy(alpha = 0.18f)
                                )
                                .border(
                                    1.dp,
                                    if (state.players.isEmpty()) BorderLight else NeonEmerald.copy(alpha = 0.45f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${state.players.size} / 20",
                                color = if (state.players.isEmpty()) TextMuted else NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.players.isEmpty()) {
                        // Clean architectural empty state (no cartoon emojis)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan.copy(alpha = 0.1f))
                                        .border(1.5.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = Strings.waitingForPlayersPrompt(lang),
                                    color = TextMuted,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    } else if (state.gameFormat == GameFormat.TEAMS) {
                        val teamsToDisplay = state.teams.ifEmpty {
                            listOf(
                                Team("team_1", if (lang == AppLanguage.PL) "Drużyna 1" else "Команда 1", "#EF4444", "01"),
                                Team("team_2", if (lang == AppLanguage.PL) "Drużyna 2" else "Команда 2", "#3B82F6", "02")
                            )
                        }
                        val gridCols = if (teamsToDisplay.size <= 4) 2 else 3

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
                                    PlayerChip(player = player)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Bottom Bar: Proceed to Package Selection ---
        val canProceed = state.players.isNotEmpty()
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        var isProceedFocused by remember { mutableStateOf(false) }
        val proceedScale by animateFloatAsState(
            targetValue = if (isProceedFocused) 1.02f else 1.0f,
            animationSpec = tween(durationMillis = 180),
            label = "btnProceedThemeScale"
        )

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .height(56.dp)
                .scale(proceedScale)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (canProceed) Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF047857)))
                    else Brush.horizontalGradient(listOf(Color(0xFF1E2638), Color(0xFF161B26)))
                )
                .border(
                    width = if (isProceedFocused) 2.dp else 1.dp,
                    color = if (isProceedFocused) Color.White else BorderLight,
                    shape = RoundedCornerShape(14.dp)
                )
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isProceedFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                    isProceedFocused = focusState.isFocused
                }
                .clickable {
                    if (canProceed) {
                        TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
                        onProceedToThemeSelect()
                    }
                }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (canProceed) Strings.btnChooseTheme(lang) else Strings.waitingForPlayersBtn(lang),
                color = if (canProceed) Color.White else TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111520))
            .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = team.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${players.size}",
                        color = accentColor,
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
                        text = if (lang == AppLanguage.PL) "Brak graczy" else "Нет участников",
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
                        PlayerChip(player = p)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerChip(player: Player) {
    val badgeText = remember(player.avatar, player.name) {
        val av = player.avatar.trim()
        if (av.length in 1..2 && av.all { it.isLetterOrDigit() }) {
            av.uppercase()
        } else {
            player.name.trim().take(2).uppercase().ifEmpty { "P1" }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark)
            .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = player.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
