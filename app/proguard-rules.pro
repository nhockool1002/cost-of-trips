# Add project specific ProGuard rules here.

# Room entities are constructed via generated code but keep them intact
# in case R8 strips fields it thinks are unused (data class copy/equals/hashCode
# reference every constructor param, so this is a defensive backstop).
-keep class com.nhockool1002.costoftrips.data.local.entity.** { *; }
