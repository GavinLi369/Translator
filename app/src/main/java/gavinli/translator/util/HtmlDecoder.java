package gavinli.translator.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import gavinli.translator.R;

/**
 * Created by GavinLi
 * on 16-11-7.
 */

public class HtmlDecoder {
    private String mHtml;
    private Context mContext;

    private ArrayList<Spanned> mSpanneds = new ArrayList<>();

    public HtmlDecoder(String html, Context context) {
        mHtml = html;
        mContext = context;
    }

    @SuppressWarnings("deprecation")
    public ArrayList<Spanned> decode() throws IndexOutOfBoundsException {
        Document document = Jsoup.parse(mHtml);
        //只显示英国翻译
        Element britishEntry = document.getElementsByClass("entry-body").get(0);
        Elements positions = britishEntry.getElementsByClass("entry-body__el clrd js-share-holder");
        for(Element position : positions) {
            Element posHeader = position.getElementsByClass("pos-header").get(0);
            buildPositionHeader(posHeader);

            for(Element senseBlock : position.getElementsByClass("sense-block")) {
                buildSenseBlock(senseBlock);
            }
        }

        return mSpanneds;
    }

    @SuppressWarnings("deprecation")
    private void buildPositionHeader(Element posHeader) {
        String positionHeader = posHeader.getElementsByClass("headword").get(0).text();
        SpannableString posHeaderSpanned = new SpannableString(positionHeader);
        posHeaderSpanned.setSpan(new RelativeSizeSpan(2f),
                0, positionHeader.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        posHeaderSpanned.setSpan(new StyleSpan(Typeface.BOLD),
                0, positionHeader.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        posHeaderSpanned.setSpan(new ForegroundColorSpan(Color.BLACK),
                0, positionHeader.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpanneds.add(posHeaderSpanned);

        SpannableStringBuilder regionBuilder = new SpannableStringBuilder();

        //词性
        String pos = posHeader.getElementsByClass("pos").get(0).text();
        SpannableString posSpanned = new SpannableString(pos + " " + "●" + " ");
        posSpanned.setSpan(new StyleSpan(Typeface.ITALIC),
                0, pos.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        posSpanned.setSpan(new RelativeSizeSpan(1.1f),
                0, pos.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        posSpanned.setSpan(new ForegroundColorSpan(Color.GRAY),
                0, pos.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        regionBuilder.append(posSpanned);

        //发音
        Elements pronInfoes = posHeader.getElementsByClass("pron-info");
        for(Element pronInfo : pronInfoes) {
            regionBuilder.append(buildPronInfo(pronInfo));
        }

        mSpanneds.add(regionBuilder);
    }

    @SuppressWarnings("deprecation")
    private SpannableStringBuilder buildPronInfo(Element pronInfo) {
        SpannableStringBuilder resultBuilder = new SpannableStringBuilder();
        //有可能有多个音标，此时region为空
        Elements regionElements = pronInfo.getElementsByClass("region");
        if(regionElements.size() != 0) {
            String region = regionElements.get(0).text();
            SpannableString regionSpanned = new SpannableString(region.toUpperCase() + "  ");
            regionSpanned.setSpan(new RelativeSizeSpan(1.1f),
                    0, region.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            regionSpanned.setSpan(new StyleSpan(Typeface.BOLD),
                    0, region.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            regionSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorRegion)),
                    0, region.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            resultBuilder.append(regionSpanned);
        }

        //音标有可能为空
        Elements pronElements = pronInfo.getElementsByClass("pron");
        if(pronElements.size() != 0) {
            String pron = pronElements.get(0).text();
            SpannableString pronSpanned = new SpannableString(pron + "  ");
            pronSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorPron)),
                    0, pron.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            resultBuilder.append(pronSpanned);
        }

        return resultBuilder;
    }

    private void buildSenseBlock(Element senseBlock) {
        Elements blockHeader = senseBlock.getElementsByClass("txt-block txt-block--alt2");
        //block header有可能为空
        if(blockHeader.size() != 0)
            mSpanneds.add(buildBlockHeader(blockHeader.get(0)));

        Elements senseBodies = senseBlock.getElementsByClass("sense-body").get(0).select("> div");
        for(int i = 0; i < senseBodies.size(); i++) {
            if(senseBodies.get(i).className().equals("def-block pad-indent")) {
                buildDefineBlock(senseBodies.get(i));
            } else if(senseBodies.get(i).className().equals("phrase-block pad-indent")) {
                buildPhraseBlock(senseBodies.get(i));
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void buildDefineBlock(Element defineBlock) {
        //解释
        Element define = defineBlock.getElementsByClass("def-block pad-indent").get(0);
        mSpanneds.add(buildDefine(define));

        //例句
        for(Element example : defineBlock.getElementsByClass("eg")) {
            mSpanneds.add(buildExample(example));
        }
    }

    @SuppressWarnings("deprecation")
    private void buildPhraseBlock(Element phraseBlock) {
        //词组
        String phraseTitle = phraseBlock.getElementsByClass("phrase-title").get(0).text();
        SpannableString phraseTitleSpanned = new SpannableString(">  " + phraseTitle);
        phraseTitleSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorPharseTitle)),
                0, phraseTitle.length() + 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        phraseTitleSpanned.setSpan(new StyleSpan(Typeface.BOLD),
                0, phraseTitle.length() + 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        phraseTitleSpanned.setSpan(new RelativeSizeSpan(1.2f),
                0, phraseTitle.length() + 3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpanneds.add(phraseTitleSpanned);

        //解释
        Element define = phraseBlock.getElementsByClass("def-block pad-indent").get(0);
        SpannableStringBuilder defineBuilder = buildDefine(define);
        defineBuilder.setSpan(new LeadingMarginSpan.Standard(40),
                0, defineBuilder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSpanneds.add(defineBuilder);

        //例句
        for(Element example : phraseBlock.getElementsByClass("eg")) {
            SpannableString exampleSpanned = buildExample(example);
            exampleSpanned.setSpan(new LeadingMarginSpan.Standard(40),
                    0, example.text().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpanneds.add(exampleSpanned);
        }
    }

    @SuppressWarnings("deprecation")
    private SpannableStringBuilder buildDefine(Element define) {
        SpannableStringBuilder defineBuilder = new SpannableStringBuilder();

        if(define.getElementsByClass("gram").size() != 0) {
            String grammer = define.getElementsByClass("gram").get(0).text();
            SpannableString grammerSpanned = new SpannableString(grammer + " ");
            grammerSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorGrammar)),
                    0, grammer.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            defineBuilder.append(grammerSpanned);
        }

        String defineText = define.getElementsByClass("def").get(0).text();
        SpannableString defineSpanned = new SpannableString(defineText);
        defineSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorDefine)),
                0, defineText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        defineSpanned.setSpan(new StyleSpan(Typeface.BOLD),
                0, defineText.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return defineBuilder.append(defineSpanned);
    }

    @SuppressWarnings("deprecation")
    private SpannableString buildExample(Element example) {
        SpannableString exampleSpanned = new SpannableString(example.text());
        exampleSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorExample)),
                0, example.text().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        exampleSpanned.setSpan(new StyleSpan(Typeface.ITALIC),
                0, example.text().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return exampleSpanned;
    }

    @SuppressWarnings("deprecation")
    private Spanned buildBlockHeader(Element blockHeader) {
        //主单词
        String headWord = blockHeader.getElementsByClass("hw").get(0).text();
        //词性
        String pos = blockHeader.getElementsByClass("pos").get(0).text();

        SpannableString blockHeaderSpanned = new SpannableString(" " + blockHeader.text() + " ");
        blockHeaderSpanned.setSpan(new BackgroundColorSpan(mContext.getResources().getColor(R.color.colorBlockHeaderBg)),
                0, blockHeader.text().length() + 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        blockHeaderSpanned.setSpan(new ForegroundColorSpan(Color.WHITE),
                0, blockHeader.text().length() + 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        blockHeaderSpanned.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorPos)),
                headWord.length() + 2, headWord.length() + pos.length() + 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        blockHeaderSpanned.setSpan(new StyleSpan(Typeface.ITALIC),
                headWord.length() + 2, headWord.length() + pos.length() + 2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return blockHeaderSpanned;
    }
}
