package gavinli.translator.util;

import android.content.Context;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.bumptech.glide.disklrucache.DiskLruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 4/27/17.
 */
public class ExplainLoader {
    private static final String CACHE_DIR = "explain";

    private Context mContext;
    private String mWord;
    private String mDictionary;
    private DiskLruCache mDiskLruCache;
    private static final long CACHE_SIZE = 10 * 1024 * 1024;

    public static ExplainLoader with(Context context) {
        return new ExplainLoader(context);
    }

    private ExplainLoader(Context context) {
        mContext = context;
        String dictionaryNum = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.key_dictionary), null);
        mDictionary = Integer.parseInt(dictionaryNum) == 0 ?
                CambirdgeApi.DICTIONARY_ENGLISH_URL : CambirdgeApi.DICTIONARY_CHINESE_URL;
    }

    public ExplainLoader search(String word) {
        mWord = word;
        return this;
    }

    public ExplainLoader dictionary(String dictionary) {
        mDictionary = dictionary;
        return this;
    }

    public List<Spanned> load() throws IOException, ExplainNotFoundException {
        if(mWord == null) throw new RuntimeException("必须设置查询单词");

        //从缓存中获取
        mDiskLruCache = DiskLruCache.open(checkOrCreateCacheDir(),
                1, 1, CACHE_SIZE);
        String key = caculateMd5(mWord + mDictionary);
        List<Spanned> explains = getExplainFromDisk(key);
        if(explains != null) return explains;

        //缓存文件未找到，从网络获取翻译
        String source = getExplainFromNetwork();

        //缓存文件
        cacheExplainToDisk(key, source);

        return new HtmlDecoder(source, mContext).decode();
    }

    private String getExplainFromNetwork() throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("不能在主线程执行网络操作");
        return CambirdgeApi.getExplainSource(mWord, mDictionary);
    }

    @Nullable
    private List<Spanned> getExplainFromDisk(String key) throws IOException, ExplainNotFoundException {
        DiskLruCache.Value value = mDiskLruCache.get(key);
        if(value == null) return null;
        File file = value.getFile(0);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)));
        StringBuilder builder = new StringBuilder();
        String temp;
        while((temp = reader.readLine()) != null) {
            builder.append(temp).append("\n");
        }
        return new HtmlDecoder(builder.toString(), mContext).decode();
    }

    private void cacheExplainToDisk(String key, String source) {
        try {
            //缓存文件存在直接返回
            DiskLruCache.Value value = mDiskLruCache.get(key);
            if(value != null) return;

            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            File file = editor.getFile(0);
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.print(source);
                writer.close();
                editor.commit();
            } finally {
                editor.abortUnlessCommitted();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File checkOrCreateCacheDir() throws IOException {
        File explainCacheDir = new File(mContext.getFilesDir() +
                File.separator + CACHE_DIR);
        if(!explainCacheDir.exists()) {
            if(!explainCacheDir.mkdirs()) {
                throw new IOException("缓存文件夹创建失败");
            }
        }
        return explainCacheDir;
    }

    private String caculateMd5(String target) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5 = digest.digest(target.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte aMd5 : md5) {
                if ((0xFF & aMd5) < 0x10) {
                    result.append('0').append(Integer.toHexString(0xFF & aMd5));
                } else {
                    result.append(Integer.toHexString(0xFF & aMd5));
                }
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
