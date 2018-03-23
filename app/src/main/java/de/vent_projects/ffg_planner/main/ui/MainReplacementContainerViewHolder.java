package de.vent_projects.ffg_planner.main.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.vent_projects.ffg_planner.R;
import de.vent_projects.ffg_planner.TimeManager;
import de.vent_projects.ffg_planner.replacement.objects.Replacement;
import io.realm.RealmList;

import static de.vent_projects.ffg_planner.CommonUtils.getAttentionInInfo;
import static de.vent_projects.ffg_planner.CommonUtils.getAttentionWithoutTitle;
import static de.vent_projects.ffg_planner.CommonUtils.isStringBlank;


public class MainReplacementContainerViewHolder extends RecyclerView.ViewHolder {
    private TextView title, info, attention;
    private View cardInfo, cardReplacement, cardAttention;
    private RecyclerView replacements;
    private boolean isInfoHidden = false, areReplacementsHidden = false, isAttentionHidden = false;

    public MainReplacementContainerViewHolder(View view) {
        super(view);
        this.title = (TextView) getView(view, R.id.text_title);
        this.info = (TextView) getView(view, R.id.text_info);
        this.attention = (TextView) getView(view, R.id.text_attention);
        this.cardInfo = getView(view, R.id.card_info);
        this.cardReplacement = getView(view, R.id.card_replacement);
        this.cardAttention = getView(view, R.id.card_attention);
        this.replacements = (RecyclerView) getView(view, R.id.list_replacements);
        if (this.replacements != null) {
            LinearLayoutManager layoutManager = new MainReplacementChildLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            this.replacements.setLayoutManager(layoutManager);
        }
    }

    public void bindReplacements(RealmList<Replacement> replacements){
        if (this.replacements != null && replacements.size() > 0){
            this.replacements.setAdapter(new MainReplacementInnerListAdapter(replacements));
            showReplacements();
        } else {
            hideReplacements();
        }
    }
    public void hideReplacements(){
        if (this.cardReplacement != null && !this.areReplacementsHidden){
            this.cardReplacement.setVisibility(View.GONE);
            this.areReplacementsHidden = true;
        }
    }
    public void showReplacements(){
        if (this.cardReplacement != null && this.areReplacementsHidden){
            this.cardReplacement.setVisibility(View.VISIBLE);
            this.areReplacementsHidden = false;
        }
    }

    public void bindTitle(String title){
        if (this.title != null){
            this.title.setText((new TimeManager(getContext())).getRelativeDate(title));
        }
    }

    public void bindInfo(String info){
        if (!isStringBlank(info)){
            String attention = getAttentionInInfo(info);
            if (!attention.equals("")) {
                this.attention.setText(getAttentionWithoutTitle(attention).trim());
                showAttention();
            } else {
                hideAttention();
            }
            String onlyInfo = info.trim().replace(attention.trim(), "");
            if (!isStringBlank(onlyInfo)) {
                this.info.setText(onlyInfo.trim());
                showInfo();
            } else {
                hideInfo();
            }
        } else {
            hideInfo();
            hideAttention();
        }
    }

    public void hideInfo(){
        if (this.cardInfo != null && !this.isInfoHidden){
            this.cardInfo.setVisibility(View.GONE);
            this.isInfoHidden = true;
        }
    }
    public void showInfo(){
        if (this.cardInfo != null && this.isInfoHidden){
            this.cardInfo.setVisibility(View.VISIBLE);
            this.isInfoHidden = false;
        }
    }
    public void hideAttention(){
        if (this.cardAttention != null && !this.isAttentionHidden){
            this.cardAttention.setVisibility(View.GONE);
            this.isAttentionHidden = true;
        }
    }
    public void showAttention(){
        if (this.cardAttention != null && this.isAttentionHidden){
            this.cardAttention.setVisibility(View.VISIBLE);
            this.isAttentionHidden = false;
        }
    }

    public Context getContext(){
        return (this.itemView == null) ? null : this.itemView.getContext();
    }

    private static View getView(View view, int id) {
        if (view == null) return null;
        return view.findViewById(id);
    }
}
