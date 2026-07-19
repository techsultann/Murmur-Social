package com.sultlab.murmur.ui.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sultlab.murmur.data.model.Group
import com.sultlab.murmur.data.model.GroupVisibility
import com.sultlab.murmur.ui.components.MurMurTopBar
import com.sultlab.murmur.ui.group.viewmodel.GroupsListViewModel
import com.sultlab.murmur.ui.theme.Dark
import com.sultlab.murmur.ui.theme.White
import murmur.composeapp.generated.resources.Res
import murmur.composeapp.generated.resources.add
import murmur.composeapp.generated.resources.ic_group
import murmur.composeapp.generated.resources.ic_restore
import murmur.composeapp.generated.resources.search
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsListScreen(
    viewModel: GroupsListViewModel,
    onGroupClick: (Group) -> Unit,
    onCreateGroup: () -> Unit,
    onRecoverGroup: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.joinedGroup) {
        uiState.joinedGroup?.let {
            onGroupClick(it)
            viewModel.consumeJoinedGroup()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MurMurTopBar(
                scrollBehavior = scrollBehavior,
                onSearchClick = {},
                title = "groups",
                description = "anonymous, together",
                actions = {
                    IconButton(onClick = onRecoverGroup) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_restore),
                            contentDescription = "recover a group",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroup,
                containerColor = Dark,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = "create group",
                    modifier = Modifier.size(25.dp)
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            GroupSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSubmitAsCode = viewModel::attemptJoinByCode,
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }

                uiState.searchQuery.isNotBlank() -> {
                    SearchResultsList(
                        results    = uiState.searchResults,
                        isSearching = uiState.isSearching,
                        onJoin     = { viewModel.attemptJoinByCode(it.joinCode) },
                    )
                }

                uiState.myGroups.isEmpty() -> {
                    GroupsEmptyState(onCreateGroup = onCreateGroup)
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.myGroups,
                            key = { it.id }
                        ) { group ->
                            GroupCard(group = group, onClick = { onGroupClick(group) })
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Toast-style feedback for join requests
        uiState.joinRequestSentGroupName?.let { name ->
            LaunchedEffect(name) {
                kotlinx.coroutines.delay(2500)
                viewModel.consumeJoinRequestToast()
            }
        }
    }
}

@Composable
private fun GroupSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmitAsCode: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "search groups or paste a join code",
                style = MaterialTheme.typography.bodyMedium
            )
                        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
                        },
        singleLine    = true,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        keyboardActions = KeyboardActions(
            onDone = { onSubmitAsCode(query) }
        ),
    )
}

@Composable
private fun GroupCard(group: Group, onClick: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape    = MaterialTheme.shapes.medium,
            modifier = Modifier.size(42.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.ic_group),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(6.dp))
                VisibilityBadge(visibility = group.visibility)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text  = buildString {
                    append("${group.memberCount} members")
                    if (group.myRole?.name == "ADMIN") append(" · admin")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VisibilityBadge(visibility: GroupVisibility) {
    val isPrivate = visibility == GroupVisibility.PRIVATE
    Surface(
        shape  = MaterialTheme.shapes.extraLarge,
        color  = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = 0.5.dp,
            color = if (isPrivate) Color(0xFF3A2E10) else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Text(
            text     = if (isPrivate) "private" else "public",
            style    = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color    = if (isPrivate) Color(0xFFEF9F27) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun SearchResultsList(
    results: List<Group>,
    isSearching: Boolean,
    onJoin: (Group) -> Unit,
) {
    if (isSearching) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
        return
    }

    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text  = "no public groups found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn {
        items(results, key = { it.id }) { group ->
            GroupCard(group = group, onClick = { onJoin(group) })
            HorizontalDivider(thickness = 0.5.dp)
        }
    }
}

@Composable
private fun GroupsEmptyState(onCreateGroup: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape    = MaterialTheme.shapes.extraLarge,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.ic_group),
                    contentDescription = null,
                    modifier  = Modifier.size(28.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("no groups yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "create a group or join one with a code to start anonymous conversations together.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick  = onCreateGroup,
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = "create a group"
            )
        }
    }
}