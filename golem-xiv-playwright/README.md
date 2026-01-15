To run Playwright’s bundled Chromium on Arch Linux, you may need to install additional libraries, because Arch is not officially supported by Playwright and the downloaded binaries are built for Ubuntu 24.04.

1. Install [`icu66`](https://aur.archlinux.org/packages/icu66-bin) and [`libffi7`](https://aur.archlinux.org/packages/libffi7) from AUR:

```bash
yay -S icu66-bin
yay -S libffi7
```
2. Build `libwebp6` from source:

```bash
git clone https://chromium.googlesource.com/webm/libwebp
cd libwebp
git checkout v0.5.2-rc2
./autogen.sh
./configure
make
sudo make install
sudo cp src/.libs/libwebp.so.6.0.2 /usr/lib/libwebp.so.6
```
