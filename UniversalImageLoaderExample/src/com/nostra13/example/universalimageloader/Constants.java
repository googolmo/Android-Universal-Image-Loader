package com.nostra13.example.universalimageloader;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class Constants {

    public static final String[] IMAGES = new String[]{
            // Heavy images
            "http://avatar1.fanfou.com/l0/01/7k/n6.jpg?1349756400",
            "http://img3.douban.com/view/photo/photo/public/p1783522312.jpg",
            "http://img3.douban.com/view/photo/photo/public/p1783526276.jpg",
            "http://img3.douban.com/view/photo/photo/public/p1783530725.jpg",
            "http://img5.douban.com/view/photo/photo/public/p1783534109.jpg",
            // Light images
            "http://tabletpcssource.com/wp-content/uploads/2011/05/android-logo.png",
            "http://simpozia.com/pages/images/stories/windows-icon.png",
            "https://si0.twimg.com/profile_images/1135218951/gmail_profile_icon3_normal.png",
            "http://www.krify.net/wp-content/uploads/2011/09/Macromedia_Flash_dock_icon.png",
            "http://radiotray.sourceforge.net/radio.png",
            "http://www.bandwidthblog.com/wp-content/uploads/2011/11/twitter-logo.png",
            "http://weloveicons.s3.amazonaws.com/icons/100907_itunes1.png",
            "http://weloveicons.s3.amazonaws.com/icons/100929_applications.png",
            "http://t2.gstatic.com/images?q=tbn:ANd9GcTJixLIo_zlOPOILuxNWc5evK333pZCH8rugaTtv3SZSfiI39T0-3vWYQ",
            "http://www.idyllicmusic.com/index_files/get_apple-iphone.png",
            "http://www.frenchrevolutionfood.com/wp-content/uploads/2009/04/Twitter-Bird.png",
            "http://3.bp.blogspot.com/-ka5MiRGJ_S4/TdD9OoF6bmI/AAAAAAAAE8k/7ydKtptUtSg/s1600/Google_Sky%2BMaps_Android.png",
            "http://www.desiredsoft.com/images/icon_webhosting.png",
            "http://goodereader.com/apps/wp-content/uploads/downloads/thumbnails/2012/01/hi-256-0-99dda8c730196ab93c67f0659d5b8489abdeb977.png",
            "http://1.bp.blogspot.com/-mlaJ4p_3rBU/TdD9OWxN8II/AAAAAAAAE8U/xyynWwr3_4Q/s1600/antivitus_free.png",
            "http://cdn3.iconfinder.com/data/icons/transformers/computer.png",
            "http://cdn.geekwire.com/wp-content/uploads/2011/04/firefox.png?7794fe",
            "https://ssl.gstatic.com/android/market/com.rovio.angrybirdsseasons/hi-256-9-347dae230614238a639d21508ae492302340b2ba",
            "http://androidblaze.com/wp-content/uploads/2011/12/tablet-pc-256x256.jpg",
            "http://www.theblaze.com/wp-content/uploads/2011/08/Apple.png",
            "http://1.bp.blogspot.com/-y-HQwQ4Kuu0/TdD9_iKIY7I/AAAAAAAAE88/3G4xiclDZD0/s1600/Twitter_Android.png",
            "http://3.bp.blogspot.com/-nAf4IMJGpc8/TdD9OGNUHHI/AAAAAAAAE8E/VM9yU_lIgZ4/s1600/Adobe%2BReader_Android.png",
            "http://cdn.geekwire.com/wp-content/uploads/2011/05/oovoo-android.png?7794fe",
            "http://icons.iconarchive.com/icons/kocco/ndroid/128/android-market-2-icon.png",
            "http://thecustomizewindows.com/wp-content/uploads/2011/11/Nicest-Android-Live-Wallpapers.png",
            "http://c.wrzuta.pl/wm16596/a32f1a47002ab3a949afeb4f",
            "http://macprovid.vo.llnwd.net/o43/hub/media/1090/6882/01_headline_Muse.jpg",
            // Special cases
            "file:///sdcard/UniversalImageLoader.png", // Image from SD card
            "assets://LivingThings.jpg", // Image from assets
            "drawable://" + R.drawable.app_icon, // Image from drawables
            "http://upload.wikimedia.org/wikipedia/ru/b/b6/Как_кот_с_мышами_воевал.png", // Link with UTF-8
            "https://www.iabti.org/images/M_images/Twitter_Image/follow_twitter_button_d.png", // Image from HTTPS
            "http://bit.ly/soBiXr", // Redirect link
            "", // Empty link
            "http://wrong.site.com/corruptedLink", // Wrong link
    };

    private Constants() {
    }

    public static class Extra {
        public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
        public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
    }
}
