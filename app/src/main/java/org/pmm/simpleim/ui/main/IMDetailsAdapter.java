package org.pmm.simpleim.ui.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.github.library.bubbleview.BubbleLinearLayout;
import com.github.library.bubbleview.BubbleTextView;

import org.pmm.simpleim.R;
import org.pmm.simpleim.db.IMDbDataBean;
import org.pmm.simpleim.utils.DateUtils;
import org.pmm.simpleim.utils.NetUtil;

import java.util.Calendar;
import java.util.List;


/**
 * 聊天内容适配器
 */
public class IMDetailsAdapter extends RecyclerView.Adapter<IMDetailsAdapter.ViewHolder> {
    private String TAG = "IMDetailsAdapter";
    private Context mContext;
    private List<IMDbDataBean> list;
    private LayoutInflater mInflater;
    private Calendar calendar;
    private int CanlenDarH = 0;//当前的时间的小时

    public IMDetailsAdapter(Context context, List<IMDbDataBean> list) {
        this.mContext = context;
        this.list = list;
        this.mInflater = LayoutInflater.from(mContext);
        calendar = Calendar.getInstance();
        CanlenDarH = calendar.get(Calendar.HOUR_OF_DAY);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.im_tem_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final IMDbDataBean chatdetails = list.get(position);

        String time = "00:00";
        try {
            time = chatdetails.getAddtime().substring(chatdetails.getAddtime().length() - 8);
            time = time.substring(0, 5);
            String day = chatdetails.getAddtime().substring(0, 10);

            //设置消息时间
            holder.topTime.setTextColor(mContext.getResources().getColor(R.color.gray_font));
            if (DateUtils.isNow(day)) {
                //日期是今天的话 只显示时间:
                holder.topTime.setText(time);//16:39
                //判断消息的时间是否为1个小时内 如果是 就隐藏时间 否则显示时间
                String[] times = time.split(":");
                if (times.length == 2) {
                    int h = Integer.parseInt(times[0]);
                    if (CanlenDarH - h >= 1) {
                        holder.topTime.setText(time);//16:39
                    } else {
                        holder.topTime.setText("");//16:39
                    }
                } else {
                    holder.topTime.setText(time);//16:39
                }
            } else if (DateUtils.isLatestWeek(day)) {
                //获取当前日期是周几
                holder.topTime.setText(DateUtils.DateToDay(day) + " " + time);// 周四 10:19
            } else {
                holder.topTime.setText(chatdetails.getAddtime());//2018-05-17 16:39
            }

            holder.tvLeftName.setText(chatdetails.getCardname());
        } catch (Exception e) {
            e.printStackTrace();
            holder.topTime.setText(time);
        }


        if (chatdetails.getCardname().equals("系统提示")) {
            //都隐藏
            holder.liLeft.setVisibility(View.GONE);
            holder.liRight.setVisibility(View.GONE);

            holder.topTime.setText(time + "\n" + "系统提示:" + chatdetails.getMessage());
            holder.topTime.setTextColor(mContext.getResources().getColor(R.color.red));
        } else if (true) {
            //这条是左边的消息
            holder.liLeft.setVisibility(View.VISIBLE);
            holder.liRight.setVisibility(View.GONE);
            //先加载左边的头像
            Glide.with(mContext).load(NetUtil.IMAGE_URL)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.ivLeftHeadimage);

            //判断消息类型
            switch ("1") {
                case "1"://文本
                    //文本显示  图片隐藏 语音隐藏
                    holder.tvLeftText.setVisibility(View.VISIBLE);
                    holder.ivLeftImage.setVisibility(View.GONE);
                    holder.vioceLeftLayout.setVisibility(View.GONE);
                    holder.vioceLeftTime.setVisibility(View.GONE);

                    //设置文本
                    holder.tvLeftText.setText(chatdetails.getMessage());
                    //文本的点击事件
                    holder.tvLeftText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("leftmsg", chatdetails.getMessage());
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("复制成功！");
                        }
                    });
                    break;
                case "2"://图片
                    //隐藏文本  隐藏语音和语音时间  显示图片
                    holder.tvLeftText.setVisibility(View.GONE);
                    holder.ivLeftImage.setVisibility(View.VISIBLE);
                    holder.vioceLeftLayout.setVisibility(View.GONE);
                    holder.vioceLeftTime.setVisibility(View.GONE);

                    Glide.with(mContext).load(NetUtil.IMAGE_URL)
                            .placeholder(R.drawable.app_logo)
                            .error(R.drawable.app_logo)
                            .into(holder.ivLeftImage);

                    //图片点击事件
                    holder.ivLeftImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    break;
                case "3"://语音

                    //隐藏文本  显示语音和语音时间  隐藏图片
                    holder.tvLeftText.setVisibility(View.GONE);
                    holder.ivLeftImage.setVisibility(View.GONE);
                    holder.vioceLeftLayout.setVisibility(View.VISIBLE);
                    holder.vioceLeftTime.setVisibility(View.VISIBLE);

                    try {
                        if (!TextUtils.isEmpty(chatdetails.getAddtime())) {
                            holder.vioceLeftTime.setText("0''");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        holder.vioceLeftTime.setText("0''");
                    }


                    holder.vioceLeftLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    break;
                default:
                    break;
            }
        } else {
            //右边消息 自己的
            //这条是右边的消息
            holder.liLeft.setVisibility(View.GONE);
            holder.liRight.setVisibility(View.VISIBLE);

            //先加载右边的头像
            Glide.with(mContext).load(NetUtil.IMAGE_URL)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .into(holder.ivRightHeadimage);

            //判断消息类型
            switch ("1") {
                case "1"://文本
                    //文本显示  图片隐藏 语音隐藏
                    holder.tvRightText.setVisibility(View.VISIBLE);
                    holder.ivRightImage.setVisibility(View.GONE);
                    holder.vioceRightLayout.setVisibility(View.GONE);
                    holder.vioceRightTime.setVisibility(View.GONE);

                    //设置文本
                    holder.tvRightText.setText(chatdetails.getMessage());
                    //文本的点击事件
                    holder.tvRightText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("leftmsg", chatdetails.getMessage());
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("复制成功！");
                        }
                    });
                    break;
                case "2"://图片
                    //隐藏文本  隐藏语音和语音时间  显示图片
                    holder.tvRightText.setVisibility(View.GONE);
                    holder.ivRightImage.setVisibility(View.VISIBLE);
                    holder.vioceRightLayout.setVisibility(View.GONE);
                    holder.vioceRightTime.setVisibility(View.GONE);

                    Glide.with(mContext).load(NetUtil.IMAGE_URL)
                            .placeholder(R.drawable.app_logo)
                            .error(R.drawable.app_logo)
                            .into(holder.ivRightImage);

                    //图片点击事件
                    holder.ivRightImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    break;
                case "3"://语音

                    //隐藏文本  显示语音和语音时间  隐藏图片
                    holder.tvRightText.setVisibility(View.GONE);
                    holder.ivRightImage.setVisibility(View.GONE);
                    holder.vioceRightLayout.setVisibility(View.VISIBLE);
                    holder.vioceRightTime.setVisibility(View.VISIBLE);


                    try {
                        if (!TextUtils.isEmpty(chatdetails.getAddtime())) {
                            holder.vioceRightTime.setText("0''");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        holder.vioceRightTime.setText("0''");
                    }


                    holder.vioceRightLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //ToastUtil.showToast("语音加载中,请稍后!");
                        }
                    });

                    break;
                default:
                    break;
            }
        }


        //布局的长按事件
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mItemOnLongClickListener.onItemOnLongClick(position);
                return true;  //为true消耗事件，false不消耗事件 继续传递
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        else return list.size();
    }

    //绑定控件
    protected class ViewHolder extends RecyclerView.ViewHolder {
        public TextView topTime;//时间
        public TextView tvLeftName;

        public ImageView ivLeftHeadimage;//左边头像
        public BubbleTextView tvLeftText;//左边文本
        public ImageView ivLeftImage;//左边图片
        public BubbleLinearLayout vioceLeftLayout;//左边语音父布局
        public TextView vioceLeftTime;//左边语音时间
        public LinearLayout liLeft;//左边父布局


        public ImageView ivRightImage;//右边图片
        public TextView vioceRightTime;//右边语音时间
        public BubbleLinearLayout vioceRightLayout;//右边语音的父布局
        public BubbleTextView tvRightText;//右边文本
        public ImageView ivRightHeadimage;//右边头像
        public LinearLayout liRight;//右边父布局

        public ViewHolder(View itemView) {
            super(itemView);
            topTime = itemView.findViewById(R.id.top_time);

            tvLeftName = itemView.findViewById(R.id.tv_name);

            ivLeftHeadimage = itemView.findViewById(R.id.iv_Left_headimage);
            tvLeftText = itemView.findViewById(R.id.tv_Left_text);
            ivLeftImage = itemView.findViewById(R.id.iv_Left_image);
            vioceLeftLayout = itemView.findViewById(R.id.vioce_Left_layout);
            vioceLeftTime = itemView.findViewById(R.id.vioce_Left_time);
            liLeft = itemView.findViewById(R.id.li_Left);

            ivRightImage = itemView.findViewById(R.id.iv_Right_image);
            vioceRightTime = itemView.findViewById(R.id.vioce_Right_time);
            vioceRightLayout = itemView.findViewById(R.id.vioce_Right_layout);
            tvRightText = itemView.findViewById(R.id.tv_right_text);
            ivRightHeadimage = itemView.findViewById(R.id.iv_Right_headimage);
            liRight = itemView.findViewById(R.id.li_right);
        }
    }


    //长按事件
    private ItemOnLongClickListener mItemOnLongClickListener;

    public interface ItemOnLongClickListener {
        void onItemOnLongClick(int position);
    }

    public void setItemOnLongClickListener(ItemOnLongClickListener itemOnLongClickListener) {
        this.mItemOnLongClickListener = itemOnLongClickListener;
    }
    //事件处理和接口
}
