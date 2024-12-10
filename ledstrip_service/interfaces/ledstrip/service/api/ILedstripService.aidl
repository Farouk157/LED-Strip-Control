package ledstrip.service.api;

import ledstrip.service.api.LedstripStatus;

@VintfStability
interface ILedstripService {
    LedstripStatus setColor(int index, int red, int green, int blue);
    LedstripStatus clear();
    LedstripStatus show();
    LedstripStatus setGlobalFade();
    LedstripStatus setRandom();
    LedstripStatus setBrightness(int brightnessPercentage);
    LedstripStatus stopAllModes();
}
