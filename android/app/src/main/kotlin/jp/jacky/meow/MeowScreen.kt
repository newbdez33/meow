package jp.jacky.meow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.jacky.meow.ui.Coral

/** What the screen needs to know beyond the cats. */
data class MeowUiState(
    val isAdFree: Boolean = false,
    val isPrivacyOptionsRequired: Boolean = false,
)

/**
 * The whole main screen (ContentView.swift): coral top bar and the cat grid, whose first row is the
 * banner slot (GridStack.swift puts Banner() first), so the banner scrolls away with the cats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowScreen(
    state: MeowUiState,
    onPlay: (Int) -> Unit,
    onShare: () -> Unit,
    onTip: () -> Unit,
    onPrivacyOptions: () -> Unit,
    onWidgets: () -> Unit = {},
    banner: @Composable () -> Unit = {},
) {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Coral,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                actions = {
                    IconButton(onClick = onWidgets, modifier = Modifier.testTag("widgets")) {
                        Icon(Icons.Outlined.Widgets, contentDescription = stringResource(R.string.widget_manage))
                    }
                    if (!state.isAdFree) {
                        IconButton(onClick = onTip, modifier = Modifier.testTag("tip")) {
                            Image(
                                painter = painterResource(R.drawable.nav_can),
                                contentDescription = stringResource(R.string.tip_button),
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                    if (state.isPrivacyOptionsRequired) {
                        IconButton(onClick = onPrivacyOptions, modifier = Modifier.testTag("privacy")) {
                            Icon(Icons.Outlined.PanTool, contentDescription = stringResource(R.string.privacy_options))
                        }
                    }
                    IconButton(onClick = onShare, modifier = Modifier.testTag("share")) {
                        Image(
                            painter = painterResource(R.drawable.nav_cat),
                            contentDescription = stringResource(R.string.share_button),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("grid"),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) { banner() }
                itemsIndexed(Cats.all) { index, cat ->
                    CatCell(
                        cat = cat,
                        selected = index == selected,
                        onClick = {
                            selected = index
                            onPlay(index)
                        },
                        modifier = Modifier.testTag("cat-$index"),
                    )
                }
            }
        }
    }
}
