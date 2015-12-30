package zeljkok.autumnsky.hikeswithaview;

/**
 * Created by zeljkok on 28/12/2015.
 */
public interface IAssetStatus
{
    public void onAssetComplete (int status, String assetName, HWVAsset.AssetType type);
}