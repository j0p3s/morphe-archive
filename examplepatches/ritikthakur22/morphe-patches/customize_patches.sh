#!/usr/bin/env bash
set -e

# Change About section links
sed -i 's|"https://morphe.software"|"https://ritikthakur.com.np"|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/AboutRoutes.java
sed -i 's|"https://github.com/morpheapp/morphe-patches/graphs/contributors"|"https://t.me/ritik7094"|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/MorpheCreditsDialog.java
sed -i 's|"https://github.com/inotia00/revanced-patches/graphs/contributors?from=3%2F1%2F2022&to=12%2F1%2F2025"|"https://github.com/ritikthakur22"|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/MorpheCreditsDialog.java
sed -i 's|morphe_settings_about_links_morphe|morphe_settings_about_links_credits|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/MorpheCreditsDialog.java

# Change the about logo URL to the user's github raw logo
sed -i 's|"https://morphe.software/favicon.svg"|"https://raw.githubusercontent.com/ritikthakur22/revanced-magisk-module/main/my_logo.png"|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/AboutRoutes.java

# Modify fetchAboutLinks to immediately return NO_CONNECTION_STATIC_LINKS so we don't fetch from Morphe API
sed -i 's|if (hasFetchedLinks()) return fetchedLinks;|return NO_CONNECTION_STATIC_LINKS;|g' extensions/shared/library/src/main/java/app/morphe/extension/shared/settings/preference/about/AboutRoutes.java

# Replace custom branding icons with my_logo.png
find patches/src/main/resources/custom-branding/ -type f -name "*.png" -exec cp ../my_logo.png {} \;

# Replace the header logo with text "By Ritik"
# We'll generate a simple image for this using ImageMagick
convert -background transparent -fill white -pointsize 100 label:'By Ritik' header_dark.png
convert -background transparent -fill black -pointsize 100 label:'By Ritik' header_light.png

# Copy into the header resources
find patches/src/main/resources/change-header/ -name "*dark*.png" -exec cp header_dark.png {} \;
find patches/src/main/resources/change-header/ -name "*light*.png" -exec cp header_light.png {} \;

# Also overwrite the XMLs with PNGs (we must rename them to .png but wait, if the patch hardcodes @drawable/morphe_header_light, we just need to delete the XML and provide a PNG of the same name)
rm -f patches/src/main/resources/change-header/drawable/morphe_header_light.xml
rm -f patches/src/main/resources/change-header/drawable/morphe_header_dark.xml

cp header_light.png patches/src/main/resources/change-header/drawable-xxxhdpi/morphe_header_light.png
cp header_dark.png patches/src/main/resources/change-header/drawable-xxxhdpi/morphe_header_dark.png

