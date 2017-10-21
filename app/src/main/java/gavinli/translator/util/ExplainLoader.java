package gavinli.translator.util;

import android.content.Context;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Spanned;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import gavinli.translator.R;
import gavinli.translator.data.source.remote.CambirdgeSource;

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

    public static ExplainLoader with(Context context) throws IOException {
        return new ExplainLoader(context);
    }

    private ExplainLoader(Context context) throws IOException {
        mContext = context;
        String dictionaryNum = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(mContext.getString(R.string.key_dictionary), "0");
        mDictionary = Integer.parseInt(dictionaryNum) == 0 ?
                CambirdgeSource.DICTIONARY_ENGLISH_URL : CambirdgeSource.DICTIONARY_CHINESE_URL;
        File dir = CreateCacheDirIfAbsent();
        mDiskLruCache = DiskLruCache.open(dir, 1, 1, CACHE_SIZE);
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
        return CambirdgeSource.getExplainSource(mWord, mDictionary);
    }

    @Nullable
    private List<Spanned> getExplainFromDisk(String key) throws IOException, ExplainNotFoundException {
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot == null) return null;
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(snapshot.getInputStream(0)));
        StringBuilder builder = new StringBuilder();
        String temp;
        while((temp = reader.readLine()) != null) {
            builder.append(temp).append("\n");
        }
        return new HtmlDecoder(builder.toString(), mContext).decode();
    }

    private void cacheExplainToDisk(String key, String source) throws IOException {
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        OutputStream out = editor.newOutputStream(0);
        try (PrintWriter writer = new PrintWriter(out)) {
            writer.print(source);
            editor.commit();
        }
    }

    private File CreateCacheDirIfAbsent() throws IOException {
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
