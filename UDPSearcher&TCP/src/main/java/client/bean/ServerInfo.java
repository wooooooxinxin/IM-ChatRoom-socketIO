package client.bean;

public class ServerInfo {
    private String _sn;
    private int _port;
    private String _address;

    public ServerInfo(String sn, int port,String address){
        this._address = address;
        this._port = port;
        this._sn = sn;
    }

    public int getPort() {
        return _port;
    }

    public String getAddress() {
        return _address;
    }

    public String getSn() {
        return _sn;
    }

    public void setAddress(String address) {
        this._address = address;
    }

    public void setPort(int port) {
        this._port = port;
    }

    public void setSn(String sn) {
        this._sn = sn;
    }

    @Override
    public String toString() {
        return "ServerInfo:{ sn='" + _sn + "\'" + ", port='" + _port + "\'" + ",address='" + _address +"\'";
    }
}
