package gavinli.translator.setting;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 服务器传来的新版App信息
 *
 * Created by gavin on 10/19/17.
 */

public class VersionEntry {
    /**
     * 版本号
     */
    final int versionCode;

    /**
     * 全量包大小(B)
     */
    final long fullSize;

    /**
     * 增量包大小(B)
     */
    final long patchSize;

    /**
     * 版本名
     */
    final String versionName;

    /**
     * 版本更新内容
     */
    final String versionLog;

    public VersionEntry(int versionCode, long fullSize, long patchSize, String versionName, String versionLog) {
        this.versionCode = versionCode;
        this.fullSize = fullSize;
        this.patchSize = patchSize;
        this.versionName = versionName;
        this.versionLog = versionLog;
    }

    /**
     * 使用JSON数据初始化
     *
     * @param jsonObject JSON对象
     *
     * @throws JSONException JSON数据出错
     */
    public VersionEntry(JSONObject jsonObject) throws JSONException {
        this.versionCode = jsonObject.getInt("versionCode");
        this.fullSize = jsonObject.getLong("fullSize");
        this.patchSize = jsonObject.getLong("patchSize");
        this.versionName = jsonObject.getString("versionName");
        this.versionLog = jsonObject.getString("versionLog");
    }
}
