/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Pty {

    @Nonnegative
    public int getCharacterWidth();

    @Nonnegative
    public int getCharacterHeight();

    @Nonnegative
    public int getPixelWidth();

    @Nonnegative
    public int getPixelHeight();

    @Nonnull
    public String getType();

    public static class Builder implements Pty {

        @Nonnull
        public static Builder pty() {
            return new Builder();
        }

        @Nonnull
        public static Pty ptyOfType(@Nonnull String type) {
            return pty().withType(type);
        }

        @Nonnegative
        private int _characterWidth = 80;
        @Nonnegative
        private int _characterHeight = 40;
        @Nonnegative
        private int _pixelWidth = 640;
        @Nonnegative
        private int _pixelHeight = 480;
        @Nonnull
        private String _type = "vt100";

        @Nonnull
        public Builder withCharacterWidth(@Nonnegative int value) {
            _characterWidth = value;
            return this;
        }

        @Nonnull
        public Builder withCharacterHeight(@Nonnegative int value) {
            _characterHeight = value;
            return this;
        }

        @Nonnull
        public Builder withPixelWidth(@Nonnegative int value) {
            _pixelWidth = value;
            return this;
        }

        @Nonnull
        public Builder withPixelHeight(@Nonnegative int value) {
            _pixelHeight = value;
            return this;
        }

        @Nonnull
        public Builder withType(@Nonnull String type) {
            _type = type;
            return this;
        }

        @Override
        @Nonnegative
        public int getCharacterWidth() {
            return _characterWidth;
        }

        public void setCharacterWidth(@Nonnegative int characterWidth) {
            _characterWidth = characterWidth;
        }

        @Override
        @Nonnegative
        public int getCharacterHeight() {
            return _characterHeight;
        }

        public void setCharacterHeight(@Nonnegative int characterHeight) {
            _characterHeight = characterHeight;
        }

        @Override
        @Nonnegative
        public int getPixelWidth() {
            return _pixelWidth;
        }

        public void setPixelWidth(@Nonnegative int pixelWidth) {
            _pixelWidth = pixelWidth;
        }

        @Override
        @Nonnegative
        public int getPixelHeight() {
            return _pixelHeight;
        }

        public void setPixelHeight(@Nonnegative int pixelHeight) {
            _pixelHeight = pixelHeight;
        }

        @Override
        @Nonnull
        public String getType() {
            return _type;
        }

        public void setType(@Nonnull String type) {
            _type = type;
        }

        @Override
        public String toString() {
            return _type + "(characters: " + _characterWidth + "x" + _characterHeight + ", pixels: " + _pixelWidth + "x" + _pixelHeight + ")";
        }

    }
}
