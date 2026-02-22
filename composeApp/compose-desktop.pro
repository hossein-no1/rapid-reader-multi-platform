-ignorewarnings
-dontnote **
#
# PDFBox references BouncyCastle APIs for optional encryption features.
# We don't require those classes for core rendering/import; suppress missing-class warnings.
-dontwarn org.bouncycastle.**
#
# Some logging implementations are optional.
-dontwarn org.apache.log4j.**
#
# Optional JDK-internal / removed modules sometimes referenced reflectively by dependencies.
-dontwarn javax.xml.bind.**
-dontwarn sun.java2d.cmm.kcms.**
