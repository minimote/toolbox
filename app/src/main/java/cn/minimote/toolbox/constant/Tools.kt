/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.dataClass.Tool


object Tools {

//    const val MY_PACKAGE_NAME = "cn.minimote.toolbox"

    // 空白工具
    val blank by lazy {
        Tool(
            id = ToolID.BLANK,
            name = "",
            packageName = "",
        )
    }


    // 应用工具
    object AppTool {
        // 微信
        object WeChat {
            private const val PACKAGE_NAME = "com.tencent.mm"
            private const val ICON_KEY = IconKey.WECHAT

            val scan by lazy {
                Tool(
                    id = ToolID.AppTool.WeChat.SCAN,
                    name = "微信-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.PACKAGE,
                    iconKey = IconKey.WECHAT_SCAN,
                    intentExtras = mapOf(
                        "LauncherUI.From.Scaner.Shortcut" to true
                    ),
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.AppTool.WeChat.PAY_CODE,
                    name = "微信-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.ACTION,
                    iconKey = IconKey.WECHAT_PAY_CODE,
                    intentAction = "com.tencent.mm.action.BIZSHORTCUT",
                    intentExtras = mapOf(
                        "LauncherUI.Shortcut.LaunchType" to "launch_type_offline_wallet"
                    ),
                )
            }

            val myCard by lazy {
                Tool(
                    id = ToolID.AppTool.WeChat.MY_CARD,
                    name = "微信-名片码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.ACTION,
                    iconKey = IconKey.WECHAT_MY_CARD,
                    intentAction = "com.tencent.mm.action.BIZSHORTCUT",
                    intentExtras = mapOf(
                        "LauncherUI.Shortcut.LaunchType" to "launch_type_my_qrcode"
                    ),
                )
            }
        }


        // 支付宝
        object Alipay {
            private const val PACKAGE_NAME = "com.eg.android.AlipayGphone"
            private const val ICON_KEY = IconKey.ALIPAY

            val scan by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.SCAN,
                    name = "支付宝-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.ALIPAY_SCAN,
                    intentUri = "alipays://platformapi/startapp?appId=10000007",
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.PAY_CODE,
                    name = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.ALIPAY_PAY_CODE,
                    intentUri = "alipays://platformapi/startapp?appId=20000056",
                )
            }

            // 手表付款码
            val payCodeWatch by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.PAY_CODE_WATCH,
                    name = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.ALIPAY_PAY_CODE,
                    intentUri = "alipays://showpage=codepay",
                )
            }

            val collectCode by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.COLLECT_CODE,
                    name = "支付宝-收款码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.ALIPAY_COLLECT_CODE,
                    intentUri = "alipays://platformapi/startapp?appId=20000123",
                )
            }

            val rideCode by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.RIDE_CODE,
                    name = "支付宝-乘车码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.ALIPAY_RIDE_CODE,
                    intentUri = "alipays://platformapi/startapp?appId=200011235",
                )
            }

            // 支付宝-支持作者
            val supportAuthor by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.SUPPORT_AUTHOR,
                    name = "支付宝-支持作者",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx13254he9xc0xpa3tdva0",
                )
            }

            // 手机充值
            val phoneRecharge by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.PHONE_RECHARGE,
                    name = "支付宝-手机充值",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=10000003",
                )
            }

            // 账单
            val bill by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BILL,
                    name = "支付宝-账单",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000003",
                )
            }

            // 银行卡
            val bankCard by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BANK_CARD,
                    name = "支付宝-银行卡",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000014",
                )
            }

            // 余额
            val balance by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BALANCE,
                    name = "支付宝-余额",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000019",
                )
            }

            // 余额宝
            val balanceBank by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BALANCE_BANK,
                    name = "支付宝-余额宝",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000032",
                )
            }

            // 转账
            val transfer by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.TRANSFER,
                    name = "支付宝-转账",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000116",
                )
            }

            // 股票
            val stock by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.STOCK,
                    name = "支付宝-股票",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000134",
                )
            }

            // 会员
            val member by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.MEMBER,
                    name = "支付宝-会员",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000160",
                )
            }

            // 通讯录
            val contacts by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.CONTACTS,
                    name = "支付宝-通讯录",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000166",
                )
            }

            // 记账
            val bookkeeping by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BOOKKEEPING,
                    name = "支付宝-记账",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000168",
                )
            }

            // 生活缴费
            val lifePayment by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.LIFE_PAYMENT,
                    name = "支付宝-生活缴费",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000193",
                )
            }

            // 花呗
            val huabei by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.HUABEI,
                    name = "支付宝-花呗",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000199",
                )
            }

            // 黄金
            val gold by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.GOLD,
                    name = "支付宝-黄金",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000218",
                )
            }

            // 总资产
            val totalAssets by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.TOTAL_ASSETS,
                    name = "支付宝-总资产",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000243",
                )
            }

            // 我的快递
            val myExpress by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.MY_EXPRESS,
                    name = "支付宝-我的快递",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000754",
                )
            }

            // 滴滴
            val didi by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.DIDI,
                    name = "支付宝-滴滴",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000778",
                )
            }

            // 基金
            val fund by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.FUND,
                    name = "支付宝-基金",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000793",
                )
            }

            // 智能助手
            val smartAssistant by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.SMART_ASSISTANT,
                    name = "支付宝-智能助手",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=20000835",
                )
            }

            // 蚂蚁森林
            val antForest by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.ANT_FOREST,
                    name = "支付宝-蚂蚁森林",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=60000002",
                )
            }

            // 蚂蚁庄园
            val antManor by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.ANT_MANOR,
                    name = "支付宝-蚂蚁庄园",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=66666674",
                )
            }

            // 商家服务
            val merchantService by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.MERCHANT_SERVICE,
                    name = "支付宝-商家服务",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=60000081",
                )
            }

            // 共享单车
            val bikeSharing by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.BIKE_SHARING,
                    name = "支付宝-共享单车",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=60000155",
                )
            }

            // 红包
            val redPacket by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.RED_PACKET,
                    name = "支付宝-红包",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=88886666",
                )
            }

            // 彩票
            val lottery by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.LOTTERY,
                    name = "支付宝-彩票",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=10000011",
                )
            }

            // 信用卡还款
            val creditCardRepayment by lazy {
                Tool(
                    id = ToolID.AppTool.Alipay.CREDIT_CARD_REPAYMENT,
                    name = "支付宝-信用卡还款",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "alipays://platformapi/startapp?appId=09999999",
                )
            }
        }


        // 云闪付
        object YunShanFu {
            private const val PACKAGE_NAME = "com.unionpay"
            private const val ICON_KEY = IconKey.YUNSHANFU

            val scan by lazy {
                Tool(
                    id = ToolID.AppTool.YunShanFu.SCAN,
                    name = "云闪付-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.YUNSHANFU_SCAN,
                    intentUri = "upwallet://native/scanCode",
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.AppTool.YunShanFu.PAY_CODE,
                    name = "云闪付-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.YUNSHANFU_PAY_CODE,
                    intentUri = "upwallet://native/codepay",
                )
            }

            // 信用卡还款
            val creditCardRepayment by lazy {
                Tool(
                    id = ToolID.AppTool.YunShanFu.CREDIT_CARD_REPAYMENT,
                    name = "云闪付-信用卡还款",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "upwallet://rn/rncredit",
                )
            }

            // 乘车码
            val rideCode by lazy {
                Tool(
                    id = ToolID.AppTool.YunShanFu.RIDE_CODE,
                    name = "云闪付-乘车码",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.YUNSHANFU_RIDE_CODE,
                    intentUri = "upwallet://rn/rnhtmlridingcode",
                )
            }

            // 签到
            val signIn by lazy {
                Tool(
                    id = ToolID.AppTool.YunShanFu.SIGN_IN,
                    name = "云闪付-签到",
                    packageName = PACKAGE_NAME,
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    intentUri = "upwallet://html/open.95516.com/s/open/html/oauth.html?responseType=code&scope=upapi_quick&appId=a3c90681a0aa4319af4de5220cd4a622&state=8c88434733b4c11cca9185194add74e14af7beff25cbfc2085bf668898e6ff944dd9413c12834f67&redirectUri=https%3A%2F%2Fyouhui.95516.com%2Fnewsign%2Funionpay%2Foauth",
                )
            }
        }


        object QQ {
            private const val PACKAGE_NAME = "com.tencent.mobileqq"
            private const val ICON_KEY = IconKey.QQ

            val zone by lazy {
                Tool(
                    id = ToolID.AppTool.QQ.ZONE,
                    name = "QQ-空间",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "mqqapi://qzone/activefeed",
                )
            }

            val profile by lazy {
                Tool(
                    id = ToolID.AppTool.QQ.PROFILE,
                    name = "QQ-个人资料",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "mqq://card/show_pslcard?src_type=internal&version=1&card_type=person",
                )
            }
        }


        object Bilibili {
            private const val PACKAGE_NAME = "tv.danmaku.bili"
            private const val ICON_KEY = IconKey.BILIBILI

            // 我的收藏
            val myCollection by lazy {
                Tool(
                    id = ToolID.AppTool.Bilibili.MY_COLLECTION,
                    name = "哔哩哔哩-我的收藏",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "bilibili://main/favorite",
                )
            }

            // 离线缓存
            val offlineCache by lazy {
                Tool(
                    id = ToolID.AppTool.Bilibili.OFFLINE_CACHE,
                    name = "哔哩哔哩-离线缓存",
                    intentType = IntentType.PACKAGE_AND_ACTIVITY,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    activityName = "tv.danmaku.bili.ui.videodownload.VideoDownloadListActivity"
                )
            }
        }


        // 网易云音乐
        object NetEaseCloudMusic {
            private const val PACKAGE_NAME = "com.netease.cloudmusic"
            private const val ICON_KEY = IconKey.NETEASE_CLOUDMUSIC

            // 每日推荐
            val dailyRecommend by lazy {
                Tool(
                    id = ToolID.AppTool.NetEaseCloudMusic.DAILY_RECOMMEND,
                    name = "网易云-每日推荐",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "orpheus://songrcmd",
                )
            }

            // 私人FM
            val privateFM by lazy {
                Tool(
                    id = ToolID.AppTool.NetEaseCloudMusic.PRIVATE_FM,
                    name = "网易云-私人FM",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "orpheus://radio",
                )
            }
        }


        // QQ音乐
        object QQMusic {
            private const val PACKAGE_NAME = "com.tencent.qqmusic"
            private const val ICON_KEY = IconKey.QQ_MUSIC

            // 每日推荐
            val dailyRecommend by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.DAILY_RECOMMEND,
                    name = "QQ音乐-每日推荐",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/ui/gedan?p={\"id\":\"4487164108\"}",
                )
            }

            // 我的收藏
            val myCollection by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.MY_COLLECTION,
                    name = "QQ音乐-我的收藏",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/ui/myTab?p=%7B%22tab%22%3A%22fav%22%7D",
                )
            }

            // 个性电台
            val personalRadio by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.PERSONAL_RADIO,
                    name = "QQ音乐-个性电台",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/media/playRadio?p=%7B%22radioId%22%3A%2299%22%7D",
                )
            }

            // 播放热歌
            val playHotSong by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.PLAY_HOT_SONG,
                    name = "QQ音乐-播放热歌",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/media/playRadio?p=%7B%22radioId%22%3A%22199%22%2C%22action%22%3A%22play%22%2C%22cache%22%3A%221%22%7D",
                )
            }

            // 积分中心
            val pointCenter by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.POINT_CENTER,
                    name = "QQ音乐-积分中心",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/ui/openUrl?p=%7B%22url%22%3A%22https%3A%2F%2Fi.y.qq.com%2Fn2%2Fm%2Fclient%2Factcenter%2Findex.html%22%7D&source=https%3A%2F%2Fi.y.qq.com%2Fn2%2Fm%2Fclient%2Factcenter%2Findex.html",
                )
            }

            // 听歌识曲
            val recognize by lazy {
                Tool(
                    id = ToolID.AppTool.QQMusic.RECOGNIZE,
                    name = "QQ音乐-听歌识曲",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "qqmusic://qq.com/ui/recognize",
                )
            }
        }


        // 抖音
        object Douyin {
            private const val PACKAGE_NAME = "com.ss.android.ugc.aweme"
            private const val ICON_KEY = IconKey.DOUYIN

            // 抖音热榜
            val hotRank by lazy {
                Tool(
                    id = ToolID.AppTool.Douyin.HOT_RANK,
                    name = "抖音-热榜",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "snssdk1128://search/trending",
                )
            }
        }


        // 京东
        object JingDong {
            private const val PACKAGE_NAME = "com.jingdong.app.mall"
            private const val ICON_KEY = IconKey.JINGDONG

            // 订单
            val order by lazy {
                Tool(
                    id = ToolID.AppTool.Jingdong.ORDER,
                    name = "京东-订单",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "openapp.jdmobile://virtual?params={category:jump,des:orderlist}",
                )
            }

            // 领京豆
            val jdBean by lazy {
                Tool(
                    id = ToolID.AppTool.Jingdong.JD_BEAN,
                    name = "京东-领京豆",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "openApp.jdMobile://virtual?params={\"category\":\"jump\",\"modulename\":\"JDReactCollectJDBeans\",\"appname\":\"JDReactCollectJDBeans\",\"ishidden\":\"true\",\"des\":\"jdreactcommon\",\"param\":{\"transparentenable\" : true,\"page\":\"collectJDBeansHomePage\"}}",
                )
            }
        }


        // 高德地图
        object AMap {
            private const val PACKAGE_NAME = "com.autonavi.minimap"
            private const val ICON_KEY = IconKey.AMAP

            // 实时公交
            val realTimeBus by lazy {
                Tool(
                    id = ToolID.AppTool.AMap.REAL_TIME_BUS,
                    name = "高德-实时公交",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "amapuri://realtimeBus/home"
                )
            }
        }


        // 美团
        object Meituan {
            private const val PACKAGE_NAME = "com.sankuai.meituan"
            private const val ICON_KEY = IconKey.MEITUAN

            // 扫一扫
            val scan by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.SCAN,
                    name = "美团-扫一扫",
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.MEITUAN_SCAN,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/scanQRCode",
                )
            }

            // 付款码
            val payCode by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.PAY_CODE,
                    name = "美团-付款码",
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.MEITUAN_PAY_CODE,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/search?q=付款码",
                )
            }

            // 单车
            val bike by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.BIKE,
                    name = "美团-单车",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/bike/home",
                )
            }

            // 搜索
            val search by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.SEARCH,
                    name = "美团-搜索",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/search",
                )
            }

            // 订单
            val order by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.ORDER,
                    name = "美团-订单",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/ordercenterlist",
                )
            }

            // 收藏
            val collection by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.COLLECTION,
                    name = "美团-收藏",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/collection/list",
                )
            }

            // 美食
            val food by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.FOOD,
                    name = "美团-美食",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/food/homepage",
                )
            }

            // 外卖
            val takeout by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.TAKEOUT,
                    name = "美团-外卖",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/takeout/homepage",
                )
            }

            // 首页
            val home by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.HOME,
                    name = "美团-首页",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/home",
                )
            }

            // 酒店
            val hotel by lazy {
                Tool(
                    id = ToolID.AppTool.Meituan.HOTEL,
                    name = "美团-酒店",
                    intentType = IntentType.SCHEME,
                    iconKey = ICON_KEY,
                    packageName = PACKAGE_NAME,
                    intentUri = "imeituan://www.meituan.com/hotel/homepage",
                )
            }
        }


        // 哈啰
        object HelloBike {
            private const val PACKAGE_NAME = "com.jingyao.easybike"

            // 扫一扫
            val scan by lazy {
                Tool(
                    id = ToolID.AppTool.HelloBike.SCAN,
                    name = "哈啰-扫一扫",
                    intentType = IntentType.SCHEME,
                    iconKey = IconKey.HELLO_BIKE_SCAN,
                    packageName = PACKAGE_NAME,
                    intentUri = "hellobike://hellobike.com/scan_qr",
                )
            }
        }
    }


    // 系统工具
    object SystemTool {

        private const val PACKAGE_NAME = "com.android.settings"

        val developerOption by lazy {
            Tool(
                id = ToolID.SystemTool.DEVELOPER_OPTION,
                name = "开发者选项",
                packageName = PACKAGE_NAME,
                activityName = $$"com.android.settings.Settings$DevelopmentSettingsDashboardActivity",
                iconKey = IconKey.DEVELOPER_OPTION,
            )
        }
        val developerOptionPixel by lazy {
            Tool(
                id = ToolID.SystemTool.DEVELOPER_OPTION_PIXEL,
                name = "开发者选项",
                packageName = PACKAGE_NAME,
                activityName = $$"com.android.settings.Settings$DevelopmentSettingsActivity",
                iconKey = IconKey.DEVELOPER_OPTION,
            )
        }

        val accessibilityOption by lazy {
            Tool(
                id = ToolID.SystemTool.ACCESSIBILITY_OPTION,
                name = "无障碍选项",
                packageName = PACKAGE_NAME,
                activityName = $$"com.android.settings.Settings$AccessibilitySettingsActivity",
                iconKey = IconKey.ACCESSIBILITY_OPTION,
            )
        }

        val systemUIDemoMode by lazy {
            Tool(
                id = ToolID.SystemTool.SYSTEM_UI_DEMO_MODE,
                name = "系统界面调节",
                packageName = "com.android.systemui",
                activityName = "com.android.systemui.DemoMode",
                iconKey = IconKey.SYSTEM_UI_DEMO_MODE,
                description = "\u3000\u3000状态栏显示秒路径：状态栏→时间",
                warningMessage = "\u3000\u3000该界面是 Android 官方提供的实验性界面，存在修改后无法恢复等风险，使用者后果自负！",
            )
        }

        val recentTask by lazy {
            Tool(
                id = ToolID.SystemTool.RECENT_TASK,
                name = "最近任务",
                packageName = "com.heytap.wearable.launcher",
                activityName = "com.android.quickstep.RecentsActivity",
                iconKey = IconKey.RECENT_TASK,
            )
        }

        val androidEasterEgg by lazy {
            Tool(
                id = ToolID.SystemTool.ANDROID_EASTER_EGG,
                name = "安卓彩蛋",
                packageName = "com.android.egg",
                activityName = "com.android.egg.landroid.MainActivity",
                iconKey = IconKey.ANDROID_EASTER_EGG,
            )
        }

    }


//    // 其他
//    object Other {
//        val woodenFish by lazy {
//            Tool(
//                id = ToolID.AppTool.WOODEN_FISH,
//                name = "木鱼",
//                intentType = IntentType.FRAGMENT,
//                packageName = MY_PACKAGE_NAME,
//                intentUri = SchemeHelper.generateSchemeFromId(ToolID.AppTool.WOODEN_FISH),
//                iconKey = IconKey.WOODEN_FISH,
//            )
//    }
}
