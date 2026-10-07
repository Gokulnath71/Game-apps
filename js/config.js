const CONFIG = {
  canvas: { width: 1280, height: 720 },

  characters: [
    {
      id: 'shadow',
      name: 'SHADOW',
      style: 'Balanced',
      color: '#0ff',
      accent: '#08a',
      stats: { speed: 7, power: 6, defense: 6, range: 5, meter: 6 },
      special: 'Shadow Strike',
      desc: 'All-rounder with shadow dash attacks'
    },
    {
      id: 'blaze',
      name: 'BLAZE',
      style: 'Rushdown',
      color: '#f80',
      accent: '#f40',
      stats: { speed: 9, power: 7, defense: 4, range: 4, meter: 7 },
      special: 'Inferno Kick',
      desc: 'Fast aggressive striker'
    },
    {
      id: 'frost',
      name: 'FROST',
      style: 'Zoner',
      color: '#8cf',
      accent: '#48f',
      stats: { speed: 5, power: 5, defense: 7, range: 9, meter: 6 },
      special: 'Ice Shard',
      desc: 'Long range projectile fighter'
    },
    {
      id: 'volt',
      name: 'VOLT',
      style: 'Mix-up',
      color: '#ff0',
      accent: '#fa0',
      stats: { speed: 8, power: 6, defense: 5, range: 6, meter: 8 },
      special: 'Thunder Clap',
      desc: 'Unpredictable electric attacks'
    },
    {
      id: 'phantom',
      name: 'PHANTOM',
      style: 'Evasive',
      color: '#a0f',
      accent: '#60a',
      stats: { speed: 8, power: 5, defense: 5, range: 5, meter: 9 },
      special: 'Phase Shift',
      desc: 'Teleports and counter attacks'
    },
    {
      id: 'titan',
      name: 'TITAN',
      style: 'Grappler',
      color: '#f44',
      accent: '#a22',
      stats: { speed: 4, power: 10, defense: 8, range: 3, meter: 5 },
      special: 'Earth Slam',
      desc: 'Slow but devastating throws'
    },
    {
      id: 'nova',
      name: 'NOVA',
      style: 'Aerial',
      color: '#f0f',
      accent: '#a0a',
      stats: { speed: 7, power: 6, defense: 5, range: 7, meter: 7 },
      special: 'Star Fall',
      desc: 'Dominates from the air'
    },
    {
      id: 'ronin',
      name: 'RONIN',
      style: 'Technical',
      color: '#0f8',
      accent: '#086',
      stats: { speed: 6, power: 7, defense: 7, range: 6, meter: 6 },
      special: 'Blade Storm',
      desc: 'Precise sword techniques'
    }
  ],

  arenas: {
    cyber: { name: 'Cyber City', floor: '#1a1a3a', sky: ['#0a0020', '#200040'], accent: '#0ff' },
    dojo: { name: 'Neon Dojo', floor: '#2a1a0a', sky: ['#1a0800', '#301000'], accent: '#f80' },
    void: { name: 'The Void', floor: '#0a0a0a', sky: ['#000', '#100020'], accent: '#a0f' },
    temple: { name: 'Cyber Temple', floor: '#1a2a1a', sky: ['#001008', '#002010'], accent: '#0f8' },
    grid: { name: 'Neon Grid', floor: '#101020', sky: ['#080818', '#101030'], accent: '#f0f' },
    storm: { name: 'Electric Storm', floor: '#1a1a2a', sky: ['#0a0a20', '#202040'], accent: '#ff0' }
  },

  difficulty: {
    easy: { reaction: 30, aggression: 0.3, blockChance: 0.2, comboChance: 0.1 },
    normal: { reaction: 18, aggression: 0.5, blockChance: 0.4, comboChance: 0.25 },
    hard: { reaction: 10, aggression: 0.7, blockChance: 0.6, comboChance: 0.4 },
    insane: { reaction: 5, aggression: 0.9, blockChance: 0.8, comboChance: 0.6 }
  },

  meterSpeed: { slow: 0.5, normal: 1, fast: 2, instant: 999 },

  defaultOptions: {
    theme: 'cyan-magenta',
    particles: 'medium',
    shake: 70,
    trails: true,
    hitflash: true,
    bgAnim: true,
    volume: 80,
    sfx: true,
    music: true,
    combo: true,
    damageNum: true,
    autoBlock: false,
    slowmo: true,
    framedata: false
  },

  defaultFight: {
    mode: 'vs-cpu',
    difficulty: 'normal',
    arena: 'cyber',
    roundTime: 60,
    roundsToWin: 2,
    healthMult: 1,
    damageMult: 1,
    meterSpeed: 'normal'
  },

  controls: {
    p1: {
      left: 'KeyA', right: 'KeyD', up: 'KeyW', down: 'KeyS',
      light: 'KeyJ', heavy: 'KeyK', block: 'KeyL',
      special: 'KeyI', throw: 'KeyO', dash: 'ShiftLeft'
    },
    p2: {
      left: 'ArrowLeft', right: 'ArrowRight', up: 'ArrowUp', down: 'ArrowDown',
      light: 'Digit1', heavy: 'Digit2', block: 'Digit3',
      special: 'Digit4', throw: 'Digit5', dash: 'Digit0'
    }
  },

  physics: {
    gravity: 0.6,
    friction: 0.85,
    jumpForce: -14,
    dashSpeed: 18,
    groundY: 580
  },

  damage: {
    light: 8,
    heavy: 18,
    special: 30,
    throw: 25,
    blockReduction: 0.7
  }
};
