package com.sultlab.murmur.ui.group

import com.sultlab.murmur.ui.group.viewmodel.CreateGroupViewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupVisibility
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import murmur.composeapp.generated.resources.ic_lock
import murmur.composeapp.generated.resources.ic_public
import murmur.composeapp.generated.resources.info
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel,
    onBack: () -> Unit,
    onCreated: (group: Group, recoveryPhrase: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdGroup) {
        val group = uiState.createdGroup
        val phrase = uiState.recoveryPhrase
        if (group != null && phrase != null) {
            onCreated(group, phrase)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "create group"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
            ) {
                Text("group name", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("e.g. night owls anonymous") },
                    singleLine = true,
                    shape  = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(18.dp))

                Text("description (optional)", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder   = { Text("what is this group about?") },
                    minLines      = 3,
                    shape         = MaterialTheme.shapes.large,
                    modifier      = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(18.dp))

                Text("visibility", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VisibilityOption(
                        icon = painterResource(Res.drawable.ic_public),
                        title = "public",
                        subtitle = "anyone can join",
                        selected = uiState.visibility == GroupVisibility.PUBLIC,
                        onClick = { viewModel.onVisibilityChange(GroupVisibility.PUBLIC) },
                        modifier = Modifier.weight(1f),
                    )
                    VisibilityOption(
                        icon = painterResource(Res.drawable.ic_lock),
                        title = "private",
                        subtitle = "admin approves",
                        selected = uiState.visibility == GroupVisibility.PRIVATE,
                        onClick = { viewModel.onVisibilityChange(GroupVisibility.PRIVATE) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(18.dp))

                InfoBanner(
                    text = "you'll get a 12-word recovery phrase after creating this group save it to regain admin access if you switch devices.",
                )

                uiState.error?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Button(
                    onClick  = viewModel::submit,
                    enabled  = uiState.canSubmit,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("create group")
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibilityOption(
    icon: Painter,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick  = onClick,
        shape    = MaterialTheme.shapes.large,
        color    = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
        border   = BorderStroke(0.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = modifier,
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape    = MaterialTheme.shapes.extraLarge,
                color    = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Surface(
        color  = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape  = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painter            = painterResource(Res.drawable.info),
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp),
            )
            Text(
                text  = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
