package com.sultlab.murmur.ui.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.GroupJoinRequest
import com.sultlab.murmur.data.model.GroupMember
import com.sultlab.murmur.data.model.GroupMemberRole
import com.sultlab.murmur.ui.group.viewmodel.GroupMembersViewModel
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.ic_check
import murmur.composeapp.generated.resources.ic_close
import murmur.composeapp.generated.resources.ic_more
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(
    viewModel: GroupMembersViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdmin = uiState.isAdmin

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "members · ${uiState.members.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                        },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        )
                    ) {
                        Icon(painterResource(Res.drawable.chevron_backward), contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (isAdmin && uiState.joinRequests.isNotEmpty()) {
                item {
                    Text(
                        text     = "join requests · ${uiState.joinRequests.size}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    )
                }
                items(
                    items = uiState.joinRequests,
                    key = { "req_${it.id}" }
                ) { request ->
                    JoinRequestRow(
                        request    = request,
                        onApprove  = { viewModel.onApproveRequest(request.deviceHash) },
                        onReject   = { viewModel.onRejectRequest(request.deviceHash) },
                    )
                }
                item { HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.background) }
            }

            items(uiState.members, key = { "mem_${it.id}" }) { member ->
                MemberRow(
                    member     = member,
                    isAdmin    = isAdmin,
                    onRemove   = { viewModel.onRemoveMember(member.deviceHash) },
                )
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: GroupMember,
    isAdmin: Boolean,
    onRemove: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape    = MaterialTheme.shapes.extraLarge,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("an", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("anonymous", style = MaterialTheme.typography.bodySmall)
            Text(
                text  = if (member.role == GroupMemberRole.ADMIN) "admin" else "member",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isAdmin && member.role != GroupMemberRole.ADMIN) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_more),
                        contentDescription = "actions"
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text    = {
                            Text(
                                text = "remove from group",
                                color = MaterialTheme.colorScheme.error
                            )
                                  },
                        onClick = { showMenu = false; showConfirm = true },
                    )
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("remove this member?") },
            text  = { Text("they will no longer be able to view or post in this group.") },
            confirmButton    = {
                TextButton(onClick = { onRemove(); showConfirm = false }) {
                    Text(
                        text = "remove",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton    = {
                TextButton(
                    onClick = { showConfirm = false }
                ) {
                    Text(
                        text = "cancel"
                    ) }
            },
        )
    }
}

@Composable
private fun JoinRequestRow(
    request: GroupJoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape    = MaterialTheme.shapes.extraLarge,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("an", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text("anonymous", style = MaterialTheme.typography.bodySmall)
            Text(
                text  = "requested ${request.requestedAt.toRelativeLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onApprove) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = "approve",
                tint = Color(0xFF1D9E75)
            )
        }
        IconButton(onClick = onReject) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "reject",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun Instant.toRelativeLabel(): String {
    val diff = Clock.System.now() - this
    return when {
        diff.inWholeMinutes < 1  -> "just now"
        diff.inWholeHours < 1    -> "${diff.inWholeMinutes}m ago"
        diff.inWholeDays < 1     -> "${diff.inWholeHours}h ago"
        else                     -> "${diff.inWholeDays}d ago"
    }
}