class ParticleSystem {
  constructor() {
    this.particles = [];
    this.damageNumbers = [];
    this.maxParticles = 200;
  }

  setIntensity(level) {
    const limits = { low: 80, medium: 200, high: 400, ultra: 800 };
    this.maxParticles = limits[level] || 200;
  }

  emit(x, y, opts = {}) {
    const count = opts.count ?? 8;
    const color = opts.color ?? '#0ff';
    const speed = opts.speed ?? 4;
    const life = opts.life ?? 30;
    const size = opts.size ?? 3;
    const spread = opts.spread ?? Math.PI * 2;

    for (let i = 0; i < count; i++) {
      if (this.particles.length >= this.maxParticles) break;
      const angle = (spread === Math.PI * 2)
        ? Math.random() * Math.PI * 2
        : opts.angle + (Math.random() - 0.5) * spread;

      this.particles.push({
        x, y,
        vx: Math.cos(angle) * speed * (0.5 + Math.random()),
        vy: Math.sin(angle) * speed * (0.5 + Math.random()),
        life, maxLife: life,
        color, size,
        gravity: opts.gravity ?? 0.1,
        shrink: opts.shrink ?? true
      });
    }
  }

  emitTrail(x, y, color) {
    if (this.particles.length >= this.maxParticles) return;
    this.particles.push({
      x: x + (Math.random() - 0.5) * 4,
      y: y + (Math.random() - 0.5) * 4,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      life: 15, maxLife: 15,
      color, size: 2,
      gravity: 0, shrink: true
    });
  }

  emitSlash(x, y, facing, color) {
    for (let i = 0; i < 12; i++) {
      const angle = (facing > 0 ? 0 : Math.PI) + (Math.random() - 0.5) * 1.2;
      this.emit(x, y, { count: 1, color, speed: 6, life: 20, size: 2, angle, spread: 0.3 });
    }
  }

  emitSpecial(x, y, color) {
    this.emit(x, y, { count: 30, color, speed: 8, life: 40, size: 4 });
    for (let ring = 0; ring < 3; ring++) {
      setTimeout(() => {
        this.emit(x, y, { count: 16, color, speed: 3 + ring * 2, life: 25, size: 3, spread: Math.PI * 2 });
      }, ring * 80);
    }
  }

  addDamageNumber(x, y, damage, blocked = false) {
    this.damageNumbers.push({
      x, y, damage,
      life: 40, maxLife: 40,
      vy: -2,
      color: blocked ? '#888' : '#ff0',
      blocked
    });
  }

  update() {
    this.particles = this.particles.filter(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += p.gravity;
      p.life--;
      return p.life > 0;
    });

    this.damageNumbers = this.damageNumbers.filter(d => {
      d.y += d.vy;
      d.vy *= 0.95;
      d.life--;
      return d.life > 0;
    });
  }

  draw(ctx) {
    ctx.save();
    for (const p of this.particles) {
      const alpha = p.life / p.maxLife;
      const size = p.shrink ? p.size * alpha : p.size;
      ctx.globalAlpha = alpha;
      ctx.fillStyle = p.color;
      ctx.shadowColor = p.color;
      ctx.shadowBlur = 8;
      ctx.beginPath();
      ctx.arc(p.x, p.y, size, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();

    ctx.save();
    ctx.font = 'bold 18px Orbitron';
    ctx.textAlign = 'center';
    for (const d of this.damageNumbers) {
      const alpha = d.life / d.maxLife;
      ctx.globalAlpha = alpha;
      ctx.fillStyle = d.color;
      ctx.shadowColor = d.color;
      ctx.shadowBlur = 6;
      ctx.fillText(d.blocked ? 'BLOCK' : `-${d.damage}`, d.x, d.y);
    }
    ctx.restore();
  }

  clear() {
    this.particles = [];
    this.damageNumbers = [];
  }
}
