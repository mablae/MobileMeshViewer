package freifunk.bremen.de.mobilemeshviewer.model.simple;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Node implements Parcelable {

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Node createFromParcel(Parcel in) {
                    return new Node(in);
                }

                public Node[] newArray(int size) {
                    return new Node[size];
                }
            };
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("position")
    @Expose
    private Position position;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("status")
    @Expose
    private Status status;

    public Node() {
    }

    public Node(Parcel in) {
        readFromParcel(in);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " [" + status + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(position, flags);
        dest.writeString(id);
        dest.writeParcelable(status, flags);
    }

    private void readFromParcel(Parcel in) {
        name = in.readString();
        position = in.readParcelable(Position.class.getClassLoader());
        id = in.readString();
        status = in.readParcelable(Status.class.getClassLoader());
    }
}