/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ToolID {

    const val BLANK = ""

    const val DYNAMIC_SHORTCUT_TITLE = "dynamic_shortcut_title"
    const val NO_DYNAMIC_SHORTCUT = "no_dynamic_shortcut"

    // 应用工具
    object AppTool {
        // 微信
        object WeChat {
            private const val URL_NAME = "wechat/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"

            const val MY_CARD = URL_NAME + "my_card"
        }

        // 支付宝
        object Alipay {
            private const val URL_NAME = "alipay/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"
            // 手表付款码
            const val PAY_CODE_WATCH = URL_NAME + "pay_code_watch"

            const val COLLECT_CODE = URL_NAME + "collect_code"

            const val RIDE_CODE = URL_NAME + "ride_code"

            // 支持作者
            const val SUPPORT_AUTHOR = URL_NAME + "support_author"

            // 手机充值
            const val PHONE_RECHARGE = URL_NAME + "phone_recharge"

            // 账单
            const val BILL = URL_NAME + "bill"

            // 银行卡
            const val BANK_CARD = URL_NAME + "bank_card"

            // 余额
            const val BALANCE = URL_NAME + "balance"

            // 余额宝
            const val BALANCE_BANK = URL_NAME + "balance_bank"

            // 转账
            const val TRANSFER = URL_NAME + "transfer"

            // 股票
            const val STOCK = URL_NAME + "stock"

            // 会员
            const val MEMBER = URL_NAME + "member"

            // 通讯录
            const val CONTACTS = URL_NAME + "contacts"

            // 记账
            const val BOOKKEEPING = URL_NAME + "bookkeeping"

            // 生活缴费
            const val LIFE_PAYMENT = URL_NAME + "life_payment"

            // 花呗
            const val HUABEI = URL_NAME + "huabei"

            // 黄金
            const val GOLD = URL_NAME + "gold"

            // 总资产
            const val TOTAL_ASSETS = URL_NAME + "total_assets"

            // 我的快递
            const val MY_EXPRESS = URL_NAME + "my_express"

            // 滴滴
            const val DIDI = URL_NAME + "didi"

            // 基金
            const val FUND = URL_NAME + "fund"

            // 智能助手
            const val SMART_ASSISTANT = URL_NAME + "smart_assistant"

            // 蚂蚁森林
            const val ANT_FOREST = URL_NAME + "ant_forest"

            // 蚂蚁庄园
            const val ANT_MANOR = URL_NAME + "ant_manor"

            // 商家服务
            const val MERCHANT_SERVICE = URL_NAME + "merchant_service"

            // 共享单车
            const val BIKE_SHARING = URL_NAME + "bike_sharing"

            // 红包
            const val RED_PACKET = URL_NAME + "red_packet"

            // 彩票
            const val LOTTERY = URL_NAME + "lottery"

            // 信用卡还款
            const val CREDIT_CARD_REPAYMENT = URL_NAME + "credit_card_repayment"

        }

        // 云闪付
        object YunShanFu {
            private const val URL_NAME = "yunshanfu/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"

            // 信用卡还款
            const val CREDIT_CARD_REPAYMENT = URL_NAME + "credit_card_repayment"

            // 乘车码
            const val RIDE_CODE = URL_NAME + "ride_code"

            // 签到
            const val SIGN_IN = URL_NAME + "sign_in"
        }

        object QQ {
            private const val URL_NAME = "qq/"

            const val ZONE = URL_NAME + "zone"

            const val PROFILE = URL_NAME + "profile"
        }

        object Bilibili {
            private const val URL_NAME = "bilibili/"

            const val MY_COLLECTION = URL_NAME + "my_collection"

            const val OFFLINE_CACHE = URL_NAME + "offline_cache"
        }

        object NetEaseCloudMusic {
            private const val URL_NAME = "netease_cloudmusic/"

            const val DAILY_RECOMMEND = URL_NAME + "daily_recommend"

            const val PRIVATE_FM = URL_NAME + "private_fm"
        }

        object QQMusic {
            private const val URL_NAME = "qqmusic/"

            const val DAILY_RECOMMEND = URL_NAME + "daily_recommend"

            const val MY_COLLECTION = URL_NAME + "my_collection"

            const val PERSONAL_RADIO = URL_NAME + "personal_radio"

            const val PLAY_HOT_SONG = URL_NAME + "play_hot_song"

            const val POINT_CENTER = URL_NAME + "point_center"

            const val RECOGNIZE = URL_NAME + "recognize"
        }

        // 抖音
        object Douyin {
            private const val URL_NAME = "douyin/"

            const val HOT_RANK = URL_NAME + "hot_rank"
        }

        // 京东
        object Jingdong {
            private const val URL_NAME = "jingdong/"

            const val ORDER = URL_NAME + "order"

            const val JD_BEAN = URL_NAME + "jd_bean"
        }

        // 高德地图
        object AMap {
            private const val URL_NAME = "amap/"

            const val REAL_TIME_BUS = URL_NAME + "real_time_bus"
        }

        // 美团
        object Meituan {
            private const val URL_NAME = "meituan/"

            const val SCAN = URL_NAME + "scan"
            const val PAY_CODE = URL_NAME + "pay_code"
            const val BIKE = URL_NAME + "bike"
            const val SEARCH = URL_NAME + "search"
            const val ORDER = URL_NAME + "order"
            const val COLLECTION = URL_NAME + "collection"
            const val FOOD = URL_NAME + "food"
            const val TAKEOUT = URL_NAME + "takeout"
            const val HOME = URL_NAME + "home"
            const val HOTEL = URL_NAME + "hotel"
        }

        object HelloBike {
            private const val URL_NAME = "hello_bike/"

            const val SCAN = URL_NAME + "scan"
        }
    }

    // 系统工具
    object SystemTool {
        private const val URL_NAME = "system/"

        const val DEVELOPER_OPTION = URL_NAME + "developer_option"
        const val DEVELOPER_OPTION_PIXEL = URL_NAME + "developer_option_pixel"

        const val ACCESSIBILITY_OPTION = URL_NAME + "accessibility_option"

        const val SYSTEM_UI_DEMO_MODE = URL_NAME + "system_UI_demo_mode"

        const val RECENT_TASK = URL_NAME + "recent_task"

        const val ANDROID_EASTER_EGG = URL_NAME + "android_easter_egg"
    }


    object Other {

        const val WOODEN_FISH = "wooden_fish"

    }
}