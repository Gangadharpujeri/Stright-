package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StreetlightViewModel
import com.example.ui.components.ReportDetailDialog
import com.example.ui.screens.CityMapScreen
import com.example.ui.screens.OutageListScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.TechnicianScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StreetlightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StreetlightApp(viewModel = viewModel)
            }
        }
    }
}

sealed class NavItem(
    val routeIndex: Int,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Report : NavItem(
        routeIndex = 0,
        title = "Report",
        selectedIcon = Icons.Filled.AddLocationAlt,
        unselectedIcon = Icons.Outlined.AddLocationAlt,
        testTag = "nav_report"
    )

    data object Outages : NavItem(
        routeIndex = 1,
        title = "Outages",
        selectedIcon = Icons.Filled.FormatListBulleted,
        unselectedIcon = Icons.Outlined.FormatListBulleted,
        testTag = "nav_outages"
    )

    data object CityMap : NavItem(
        routeIndex = 2,
        title = "GIS Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        testTag = "nav_map"
    )

    data object Technician : NavItem(
        routeIndex = 3,
        title = "Field Ops",
        selectedIcon = Icons.Filled.Engineering,
        unselectedIcon = Icons.Outlined.Engineering,
        testTag = "nav_tech"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreetlightApp(viewModel: StreetlightViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val selectedReport by viewModel.selectedReport.collectAsState()

    val openCount = allReports.count { it.status == "SUBMITTED" }

    val navItems = listOf(
        NavItem.Report,
        NavItem.Outages,
        NavItem.CityMap,
        NavItem.Technician
    )

    // Handle system back button
    BackHandler(enabled = selectedTab != 0 || selectedReport != null) {
        if (selectedReport != null) {
            viewModel.selectReport(null)
        } else {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "StreetLight Alert",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            text = "City Sector 4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedTab == item.routeIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.routeIndex) },
                        icon = {
                            if (item is NavItem.Outages && openCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text(openCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> ReportScreen(
                    viewModel = viewModel,
                    onNavigateToFeed = { viewModel.selectTab(1) }
                )
                1 -> OutageListScreen(
                    viewModel = viewModel,
                    onNavigateToReport = { viewModel.selectTab(0) }
                )
                2 -> CityMapScreen(viewModel = viewModel)
                3 -> TechnicianScreen(viewModel = viewModel)
            }

            // Modal detail dialog when a report is selected anywhere in the app
            selectedReport?.let { report ->
                ReportDetailDialog(
                    report = report,
                    onDismiss = { viewModel.selectReport(null) },
                    onUpvote = { reportId -> viewModel.upvoteReport(reportId) }
                )
            }
        }
    }
}
