const AudioEngine = {
  ctx: null,
  options: { volume: 0.8, sfx: true, music: true },

  init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
  },

  setOptions(opts) {
    this.options = { ...this.options, ...opts };
  },

  play(type, opts = {}) {
    if (!this.options.sfx) return;
    this.init();
    const vol = (opts.volume ?? 1) * (this.options.volume / 100);

    switch (type) {
      case 'hit': this._hit(vol, opts.pitch ?? 1); break;
      case 'block': this._block(vol); break;
      case 'whoosh': this._whoosh(vol); break;
      case 'special': this._special(vol); break;
      case 'jump': this._jump(vol); break;
      case 'dash': this._dash(vol); break;
      case 'ko': this._ko(vol); break;
      case 'round': this._round(vol); break;
      case 'select': this._select(vol); break;
      case 'menu': this._menu(vol); break;
    }
  },

  _osc(freq, type, duration, vol, fade = true) {
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = type;
    osc.frequency.value = freq;
    gain.gain.value = vol * 0.3;
    if (fade) gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + duration);
  },

  _hit(vol, pitch) {
    this._osc(150 * pitch, 'square', 0.1, vol);
    this._osc(80 * pitch, 'sawtooth', 0.15, vol * 0.5);
  },

  _block(vol) {
    this._osc(300, 'square', 0.05, vol);
    this._osc(200, 'sine', 0.08, vol * 0.6);
  },

  _whoosh(vol) {
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(800, this.ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(200, this.ctx.currentTime + 0.15);
    gain.gain.value = vol * 0.15;
    gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.15);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 0.15);
  },

  _special(vol) {
    [440, 554, 659, 880].forEach((f, i) => {
      setTimeout(() => this._osc(f, 'square', 0.2, vol * 0.4), i * 60);
    });
  },

  _jump(vol) {
    this._osc(400, 'sine', 0.1, vol * 0.3);
    this._osc(600, 'sine', 0.08, vol * 0.2);
  },

  _dash(vol) {
    this._whoosh(vol * 1.5);
  },

  _ko(vol) {
    [880, 660, 440, 220].forEach((f, i) => {
      setTimeout(() => this._osc(f, 'square', 0.4, vol * 0.5), i * 150);
    });
  },

  _round(vol) {
    this._osc(523, 'square', 0.3, vol);
    setTimeout(() => this._osc(784, 'square', 0.5, vol), 300);
  },

  _select(vol) {
    this._osc(660, 'sine', 0.08, vol * 0.3);
  },

  _menu(vol) {
    this._osc(440, 'sine', 0.06, vol * 0.2);
  }
};
