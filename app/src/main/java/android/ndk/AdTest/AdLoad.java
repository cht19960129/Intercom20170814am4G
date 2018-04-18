package android.ndk.AdTest;

public class AdLoad
{
    static
    {
        System.loadLibrary("FpCore");
    }

    public native int FPMatch(byte[] p_pTemplate1, byte[] p_pTemplate2, int p_nSecLevel, int[] p_nMatchRes, byte[] p_pTempBuffer);
}