package app.morphe.extension.network;

@SuppressWarnings("unused")
public class NetworkCapabilities {
  public static boolean hasTransport(android.net.NetworkCapabilities self, int transportType) {
    if (transportType == android.net.NetworkCapabilities.TRANSPORT_VPN) {
      return false;
    }

    return self.hasTransport(transportType);
  }
}
