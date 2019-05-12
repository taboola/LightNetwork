package com.taboola.lightnetwork.url_components;

import android.text.TextUtils;

public class UrlParameter {
    public String mKey;
    public String mValue;

    public UrlParameter(Object key, Object value) {
        mKey = String.valueOf(key);
        mValue = String.valueOf(value);
    }

    /**
     * A parameter is only valid if it has a non-null or empty key.
     */
    public boolean isValid(){
        return !TextUtils.isEmpty(mKey);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof UrlParameter) {
            UrlParameter other = (UrlParameter) obj;

            if (other.mKey != null && other.mKey.equals(this.mKey)) { //No need to null check, parameters are validate to have a non empty key before entering data structure.
                return true;
            }
        }
        return super.equals(obj);
    }

    /**
     * Java always optimizes calls for equals by going through 'hashCode()' first.
     * It will only call equals() for two objects that have the same hashCode.
     *
     * It is recommended to create a hashCode based on the class name
     * Source: https://stackoverflow.com/questions/17919464/hashcode-and-equals-method
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

}
