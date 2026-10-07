const FighterState = {
  IDLE: 'idle', WALK: 'walk', JUMP: 'jump', CROUCH: 'crouch',
  ATTACK_LIGHT: 'attack_light', ATTACK_HEAVY: 'attack_heavy',
  BLOCK: 'block', SPECIAL: 'special', THROW: 'throw',
  DASH: 'dash', HIT: 'hit', KO: 'ko', LANDING: 'landing'
};

class Fighter {
  constructor(charData, x, facing, playerNum, fightSettings) {
    this.char = charData;
    this.x = x;
    this.y = CONFIG.physics.groundY;
    this.vx = 0;
    this.vy = 0;
    this.facing = facing;
    this.playerNum = playerNum;
    this.width = 40;
    this.height = 80;
    this.crouchHeight = 50;

    const hpMult = fightSettings.healthMult || 1;
    this.maxHealth = Math.round(100 * hpMult);
    this.health = this.maxHealth;
    this.maxMeter = 100;
    this.meter = 0;

    this.state = FighterState.IDLE;
    this.stateTimer = 0;
    this.animFrame = 0;
    this.onGround = true;
    this.invincible = 0;
    this.hitStun = 0;
    this.blockStun = 0;
    this.comboCount = 0;
    this.comboTimer = 0;
    this.lastHitBy = null;

    this.damageMult = fightSettings.damageMult || 1;
    this.meterRate = CONFIG.meterSpeed[fightSettings.meterSpeed] || 1;

    this.speed = charData.stats.speed;
    this.power = charData.stats.power;
    this.defense = charData.stats.defense;

    this.attackBox = null;
    this.hurtBox = { x: 0, y: 0, w: 0, h: 0 };
    this.projectiles = [];
    this.trail = [];
  }

  get isAlive() { return this.health > 0 && this.state !== FighterState.KO; }

  get isAttacking() {
    return [FighterState.ATTACK_LIGHT, FighterState.ATTACK_HEAVY,
      FighterState.SPECIAL, FighterState.THROW].includes(this.state);
  }

  get isBlocking() { return this.state === FighterState.BLOCK; }

  get currentHeight() {
    return this.state === FighterState.CROUCH ? this.crouchHeight : this.height;
  }

  updateHurtBox() {
    const h = this.currentHeight;
    this.hurtBox = {
      x: this.x - this.width / 2,
      y: this.y - h,
      w: this.width,
      h: h
    };
  }

  updateAttackBox() {
    if (!this.isAttacking || this.stateTimer > 15) {
      this.attackBox = null;
      return;
    }

    let range = 30;
    let damage = CONFIG.damage.light;
    let height = 40;
    let yOffset = -50;

    switch (this.state) {
      case FighterState.ATTACK_LIGHT:
        range = 25 + this.char.stats.range * 2;
        damage = CONFIG.damage.light;
        break;
      case FighterState.ATTACK_HEAVY:
        range = 35 + this.char.stats.range * 3;
        damage = CONFIG.damage.heavy;
        height = 50;
        break;
      case FighterState.SPECIAL:
        range = 60 + this.char.stats.range * 4;
        damage = CONFIG.damage.special;
        height = 60;
        yOffset = -55;
        break;
      case FighterState.THROW:
        range = 20;
        damage = CONFIG.damage.throw;
        height = 50;
        break;
    }

    damage = Math.round(damage * (this.power / 6) * this.damageMult);

    this.attackBox = {
      x: this.facing > 0 ? this.x + 10 : this.x - 10 - range,
      y: this.y + yOffset,
      w: range,
      h: height,
      damage,
      type: this.state,
      owner: this
    };
  }

  canAct() {
    return this.isAlive && this.hitStun <= 0 && this.blockStun <= 0 &&
      this.state !== FighterState.KO && this.state !== FighterState.HIT;
  }

  startAttack(type) {
    if (!this.canAct() || this.isAttacking) return false;

    if (type === FighterState.SPECIAL) {
      if (this.meter < 50) return false;
      this.meter -= 50;
    }

    this.state = type;
    this.stateTimer = 0;
    this.vx = 0;
    AudioEngine.play(type === FighterState.SPECIAL ? 'special' : 'whoosh');
    return true;
  }

  startBlock() {
    if (!this.canAct() || !this.onGround) return;
    this.state = FighterState.BLOCK;
    this.vx *= 0.5;
  }

  stopBlock() {
    if (this.state === FighterState.BLOCK) this.state = FighterState.IDLE;
  }

  dash(direction) {
    if (!this.canAct() || !this.onGround) return false;
    this.state = FighterState.DASH;
    this.stateTimer = 0;
    this.vx = direction * CONFIG.physics.dashSpeed * (this.speed / 7);
    this.invincible = 8;
    AudioEngine.play('dash');
    return true;
  }

  jump() {
    if (!this.onGround || !this.canAct()) return;
    this.vy = CONFIG.physics.jumpForce * (this.speed / 7 + 0.5);
    this.onGround = false;
    this.state = FighterState.JUMP;
    AudioEngine.play('jump');
  }

  takeDamage(damage, attacker, attackType) {
    if (this.invincible > 0 || !this.isAlive) return false;

    if (this.isBlocking && this.onGround) {
      const reduced = Math.round(damage * CONFIG.damage.blockReduction * (6 / this.defense));
      this.health -= reduced;
      this.blockStun = 10;
      this.meter += 5 * this.meterRate;
      AudioEngine.play('block');
      return { blocked: true, damage: reduced };
    }

    this.health = Math.max(0, this.health - damage);
    this.hitStun = attackType === FighterState.ATTACK_HEAVY ? 20 : 12;
    this.state = FighterState.HIT;
    this.stateTimer = 0;
    this.vx = -this.facing * 4;
    this.invincible = 15;
    this.lastHitBy = attacker;

    if (attacker) {
      attacker.comboCount++;
      attacker.comboTimer = 90;
      attacker.meter += 8 * this.meterRate;
    }

    this.meter += 3 * this.meterRate;
    AudioEngine.play('hit', { pitch: 0.8 + Math.random() * 0.4 });

    if (this.health <= 0) {
      this.state = FighterState.KO;
      AudioEngine.play('ko');
    }

    return { blocked: false, damage };
  }

  update(input, opponent) {
    this.animFrame++;
    if (this.invincible > 0) this.invincible--;
    if (this.hitStun > 0) this.hitStun--;
    if (this.blockStun > 0) this.blockStun--;
    if (this.comboTimer > 0) { this.comboTimer--; if (this.comboTimer <= 0) this.comboCount = 0; }

    this.stateTimer++;

    if (this.state === FighterState.HIT && this.hitStun <= 0) {
      this.state = this.onGround ? FighterState.IDLE : FighterState.JUMP;
    }

    if (this.isAttacking && this.stateTimer > 25) {
      this.state = FighterState.IDLE;
      this.attackBox = null;
    }

    if (this.state === FighterState.DASH && this.stateTimer > 12) {
      this.state = FighterState.IDLE;
      this.vx *= 0.3;
    }

    if (input && this.canAct()) this._handleInput(input, opponent);

    this.vy += CONFIG.physics.gravity;
    this.x += this.vx;
    this.y += this.vy;

    if (this.onGround) this.vx *= CONFIG.physics.friction;
    else this.vx *= 0.98;

    const ground = CONFIG.physics.groundY;
    if (this.y >= ground) {
      this.y = ground;
      this.vy = 0;
      if (!this.onGround) this.onGround = true;
      if (this.state === FighterState.JUMP) this.state = FighterState.IDLE;
    } else {
      this.onGround = false;
    }

    this.x = Math.max(60, Math.min(CONFIG.canvas.width - 60, this.x));

    if (opponent && this.canAct() && !this.isAttacking) {
      if (this.x < opponent.x) this.facing = 1;
      else if (this.x > opponent.x) this.facing = -1;
    }

    this.updateHurtBox();
    this.updateAttackBox();
    this._updateProjectiles();
    this._updateTrail();
  }

  _handleInput(input, opponent) {
    const moveSpeed = this.speed * 0.8;

    if (input.block) {
      this.startBlock();
      return;
    } else if (this.state === FighterState.BLOCK) {
      this.stopBlock();
    }

    if (input.dash && this.onGround) {
      const dir = (input.left ? -1 : 0) + (input.right ? 1 : 0) || this.facing;
      this.dash(dir);
      return;
    }

    if (input.special) { this.startAttack(FighterState.SPECIAL); return; }
    if (input.heavy) { this.startAttack(FighterState.ATTACK_HEAVY); return; }
    if (input.light) { this.startAttack(FighterState.ATTACK_LIGHT); return; }
    if (input.throw && opponent) {
      const dist = Math.abs(this.x - opponent.x);
      if (dist < 50) this.startAttack(FighterState.THROW);
      return;
    }

    if (this.isAttacking || this.state === FighterState.DASH) return;

    if (input.up && this.onGround) { this.jump(); return; }

    if (input.down && this.onGround) {
      this.state = FighterState.CROUCH;
      this.vx *= 0.7;
      return;
    }

    if (input.left) {
      this.vx = -moveSpeed;
      this.state = FighterState.WALK;
    } else if (input.right) {
      this.vx = moveSpeed;
      this.state = FighterState.WALK;
    } else if (this.onGround && this.state !== FighterState.CROUCH) {
      this.state = FighterState.IDLE;
    }
  }

  _updateProjectiles() {
    if (this.char.id === 'frost' && this.state === FighterState.SPECIAL && this.stateTimer === 5) {
      this.projectiles.push({
        x: this.x + this.facing * 30,
        y: this.y - 50,
        vx: this.facing * 12,
        vy: 0,
        damage: Math.round(CONFIG.damage.special * 0.6 * this.damageMult),
        life: 60,
        color: this.char.color,
        owner: this
      });
    }

    this.projectiles = this.projectiles.filter(p => {
      p.x += p.vx;
      p.life--;
      return p.life > 0 && p.x > 0 && p.x < CONFIG.canvas.width;
    });
  }

  _updateTrail() {
    if (this.state === FighterState.DASH || this.state === FighterState.SPECIAL) {
      this.trail.push({ x: this.x, y: this.y - 40, life: 10, color: this.char.color });
    }
    this.trail = this.trail.filter(t => { t.life--; return t.life > 0; });
  }

  draw(ctx, showTrails) {
    if (showTrails) {
      for (const t of this.trail) {
        ctx.globalAlpha = t.life / 10 * 0.4;
        ctx.fillStyle = t.color;
        ctx.shadowColor = t.color;
        ctx.shadowBlur = 10;
        ctx.fillRect(t.x - 15, t.y - 20, 30, 40);
      }
      ctx.globalAlpha = 1;
    }

    if (this.invincible > 0 && Math.floor(this.invincible / 2) % 2) return;

    const h = this.currentHeight;
    const baseY = this.y;

    ctx.save();
    ctx.translate(this.x, baseY);
    if (this.facing < 0) ctx.scale(-1, 1);

    const color = this.char.color;
    const accent = this.char.accent;

    ctx.shadowColor = color;
    ctx.shadowBlur = 15;

    // Body
    ctx.fillStyle = color;
    ctx.globalAlpha = 0.9;
    ctx.fillRect(-12, -h + 10, 24, h - 20);

    // Head
    ctx.beginPath();
    ctx.arc(0, -h + 8, 14, 0, Math.PI * 2);
    ctx.fill();

    // Headband / mask
    ctx.fillStyle = accent;
    ctx.fillRect(-16, -h + 2, 32, 6);

    // Eyes (glowing)
    ctx.fillStyle = '#fff';
    ctx.shadowBlur = 8;
    ctx.fillRect(4, -h + 6, 6, 3);

    // Arms
    const armSwing = this.isAttacking ? this.stateTimer * 0.5 : Math.sin(this.animFrame * 0.1) * 3;
    ctx.fillStyle = color;
    ctx.fillRect(8, -h + 30 + armSwing, 8, 25);

    if (this.isAttacking && this.stateTimer < 12) {
      ctx.fillStyle = accent;
      ctx.shadowBlur = 20;
      ctx.globalAlpha = 0.8;
      const reach = this.state === FighterState.SPECIAL ? 50 : 30;
      ctx.fillRect(12, -h + 35, reach, 6);
    }

    // Legs
    const legOffset = this.state === FighterState.WALK ? Math.sin(this.animFrame * 0.2) * 8 : 0;
    ctx.fillStyle = accent;
    ctx.globalAlpha = 0.7;
    ctx.fillRect(-10, -20, 8, 20 + legOffset);
    ctx.fillRect(2, -20, 8, 20 - legOffset);

    // Block shield
    if (this.isBlocking) {
      ctx.strokeStyle = color;
      ctx.lineWidth = 3;
      ctx.globalAlpha = 0.6;
      ctx.beginPath();
      ctx.arc(20, -h / 2, 25, -Math.PI / 3, Math.PI / 3);
      ctx.stroke();
    }

    // Special aura
    if (this.state === FighterState.SPECIAL) {
      ctx.globalAlpha = 0.3 + Math.sin(this.animFrame * 0.3) * 0.2;
      ctx.strokeStyle = accent;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(0, -h / 2, 40 + this.stateTimer, 0, Math.PI * 2);
      ctx.stroke();
    }

    ctx.restore();

    // Projectiles
    for (const p of this.projectiles) {
      ctx.save();
      ctx.fillStyle = p.color;
      ctx.shadowColor = p.color;
      ctx.shadowBlur = 15;
      ctx.beginPath();
      ctx.arc(p.x, p.y, 8, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    }
  }
}
