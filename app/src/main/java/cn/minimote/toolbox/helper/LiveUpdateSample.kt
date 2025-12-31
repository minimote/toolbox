/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


//@RequiresApi(Build.VERSION_CODES.BAKLAVA)
//@Composable
//fun LiveUpdateSample() {
//    val notificationManager =
//        LocalContext.current.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    SnackbarNotificationManager.initialize(
//        LocalContext.current.applicationContext,
//        notificationManager
//    )
//    val scope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//    Scaffold(
//        snackbarHost = {
//            SnackbarHost(hostState = snackbarHostState)
//        },
//    ) { contentPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(contentPadding),
//        ) {
//            NotificationPermission()
//            Spacer(modifier = Modifier.height(4.dp))
//            NotificationPostPromotedPermission()
//            Text(stringResource(R.string.tool_name))
//            Spacer(modifier = Modifier.height(4.dp))
//            Button(
//                onClick = {
//                    onCheckout()
//                    scope.launch {
//                        snackbarHostState.showSnackbar("Order placed")
//                    }
//                },
//            ) {
//                Text("Checkout")
//            }
//        }
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.BAKLAVA)
//fun onCheckout() {
//    SnackbarNotificationManager.start()
//}
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun NotificationPermission() {
//    @SuppressLint("InlinedApi") // Granted at install time on API <33.
//    val notificationPermissionState = rememberPermissionState(
//        android.Manifest.permission.POST_NOTIFICATIONS,
//    )
//    if(!notificationPermissionState.status.isGranted) {
//        NotificationPermissionCard(
//            shouldShowRationale = notificationPermissionState.status.shouldShowRationale,
//            onGrantClick = {
//                notificationPermissionState.launchPermissionRequest()
//            },
//            modifier = Modifier
//                .fillMaxWidth(),
//            permissionStringResourceId = R.string.tool_name,
//            permissionRationalStringResourceId = R.string.tool_name,
//        )
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.BAKLAVA)
//@Composable
//fun NotificationPostPromotedPermission() {
//    val context = LocalContext.current
//    var isPostPromotionsEnabled by remember { mutableStateOf(SnackbarNotificationManager.isPostPromotionsEnabled()) }
//    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
//        isPostPromotionsEnabled = SnackbarNotificationManager.isPostPromotionsEnabled()
//    }
//    if(!isPostPromotionsEnabled) {
//        Text(
//            text = stringResource(R.string.tool_name),
//            modifier = Modifier.padding(horizontal = 10.dp),
//        )
//        Button(
//            onClick = {
//                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
//                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
//                }
//                context.startActivity(intent)
//            },
//        ) {
//            Text(text = stringResource(R.string.tool_name))
//        }
//    }
//}
//
//@Composable
//private fun NotificationPermissionCard(
//    shouldShowRationale: Boolean,
//    onGrantClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    permissionStringResourceId: Int,
//    permissionRationalStringResourceId: Int,
//) {
//    Card(
//        modifier = modifier,
//    ) {
//        Text(
//            text = stringResource(permissionStringResourceId),
//            modifier = Modifier.padding(16.dp),
//        )
//        if(shouldShowRationale) {
//            Text(
//                text = stringResource(permissionRationalStringResourceId),
//                modifier = Modifier.padding(horizontal = 10.dp),
//            )
//        }
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(10.dp),
//            contentAlignment = Alignment.BottomEnd,
//        ) {
//            Button(onClick = onGrantClick) {
//                Text(text = stringResource(R.string.tool_name))
//            }
//        }
//    }
//}