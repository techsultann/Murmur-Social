package com.sultlab.murmur.ui.group

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultlab.murmur.data.model.Group
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.ic_key
import org.jetbrains.compose.resources.painterResource

@Composable
fun RecoveryPhraseScreen(
    group: Group,
    recoveryPhrase: String,
    onCopyPhrase: () -> Unit,
    onShareJoinCode: () -> Unit,
    onContinue: () -> Unit,
) {
    val words = recoveryPhrase.split(" ")

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_key),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "save your recovery phrase",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = "this is the only time you'll see this phrase. write it down it's the only way to recover admin access if you switch devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                items(words.size) { index ->
                    PhraseChip(index = index + 1, word = words[index])
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onCopyPhrase,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                ) { Text("copy phrase") }

                OutlinedButton(
                    onClick  = onShareJoinCode,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f),
                ) { Text("share join code") }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick  = onContinue,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("i've saved it, continue")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PhraseChip(index: Int, word: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text     = index.toString(),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.width(16.dp),
            )
            Text(text = word, style = MaterialTheme.typography.bodySmall)
        }
    }
}