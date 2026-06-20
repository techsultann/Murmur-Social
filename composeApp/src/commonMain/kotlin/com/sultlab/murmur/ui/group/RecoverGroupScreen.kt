package com.sultlab.murmur.ui.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.ui.group.viewmodel.RecoverGroupViewModel
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.chevron_backward
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverGroupScreen(
    viewModel: RecoverGroupViewModel,
    onBack: () -> Unit,
    onRecovered: (Group) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.recoveredGroup) {
        uiState.recoveredGroup?.let {
            onRecovered(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("recover group") },
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
                .padding(padding)
                .padding(18.dp),
        ) {
            Text(
                text = "enter your 12-word recovery phrase to regain admin access to your group.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.phraseInput,
                onValueChange = viewModel::onPhraseChange,
                placeholder = { Text("word1 word2 ... word12") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = MaterialTheme.shapes.large,
            )

            uiState.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = viewModel::submit,
                enabled = uiState.phraseInput.isNotBlank() && !uiState.isRecovering,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isRecovering) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("recover group")
                }
            }
        }
    }
}
