package at.pardus.android.webview.gm.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceHelper {

  public static byte[] getRawResource(Context context, int id) throws IOException {
    Resources             resources   = context.getResources();
    InputStream           stream_in   = resources.openRawResource(id);
    ByteArrayOutputStream stream_out  = new ByteArrayOutputStream();
    byte[]                data_buffer = new byte[4 * 1024];

    try {
      int read;
      do {
        read = stream_in.read(data_buffer, 0, data_buffer.length);
        if(read == -1) {
          break;
        }
        stream_out.write(data_buffer, 0, read);
      } while(true);

      return stream_out.toByteArray();
    }
    finally {
      stream_in.close();
    }
  }

  public static String getRawStringResource(Context context, int id, Charset encoding) throws IOException {
    return new String(getRawResource(context, id), encoding);
  }

  public static String getRawStringResource(Context context, int id) throws IOException {
    return getRawStringResource(context, id, Charset.forName("UTF-8"));
  }
}
