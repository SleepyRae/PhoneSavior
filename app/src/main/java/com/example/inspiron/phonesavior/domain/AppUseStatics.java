package com.example.inspiron.phonesavior.domain;

import com.example.inspiron.phonesavior.view.SlideView;

/*
 * 应用程序的信息：基本信息加上使用信息
 * 集成基本信息类APPInfo
 */
public class AppUseStatics extends AppInfo implements Comparable<AppUseStatics> {
    private int useFreq; // 使用频率，以次数为单位
    private int useTime; // 使用时长，以秒为单位
    private int weight; // 权重，用于排序
    private SlideView slideView; // 自定义的滑动控件

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getUseFreq() {
        return useFreq;
    }

    public void setUseFreq(int useFreq) {
        this.useFreq = useFreq;
    }

    public int getUseTime() {
        return useTime;
    }

    public void setUseTime(int useTime) {
        this.useTime = useTime;
    }

    public SlideView getSlideView() {
        return slideView;
    }

    public void setSlideView(SlideView slideView) {
        this.slideView = slideView;
    }

    /**
     * 实现其按权重和是否是系统应用进行排序
     *
     * 依次按权重值，使用频率，使用时间，是否是第三方应用进行排序
     *
     * a negative integer if this instance is less than another; a positive
     * integer if this instance is greater than another; 0 if this instance has
     * the same order as another.
     *
     * Collection.sort实现的是递增排序，此处需要的是递减排序，所以在compareTo实现时使用相反的逻辑
     *
     * 后来优化了一下，直接计算权值，按权值进行比较 权值计算系统应用初始化为0，三方应用初始化为1，保证三方应用总是排在系统应用前面 权值 =
     * 使用时间长度 + 使用次数
     *
     * 比较函数复杂总是会导致意想不到的错误顺序,其次还会犯一些比较器冲突的错误
     * 比如没有对相等情形进行判断的时候就会抛出如下异常：
     * Comparison method violates its general contract
     */
    @Override
    public int compareTo(AppUseStatics another) {

        if(this.weight > another.weight)
            return -1;
        else if(this.weight < another.weight)
            return 1;
        else
            return 0;
    }

}

