(function (w) {
  const SpaceBG = {
    init(selector) {
      const cvs = document.querySelector(selector);
      if (!cvs) return;
      const ctx = cvs.getContext('2d');

      let width, height;
      const dpr = Math.max(1, w.devicePixelRatio || 1);
      const stars = [];
      const STAR_COUNT = 300; // tweak for more/less stars

      function resize() {
        width = cvs.clientWidth;
        height = cvs.clientHeight;
        cvs.width = Math.floor(width * dpr);
        cvs.height = Math.floor(height * dpr);
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0); // draw in CSS pixels
      }

      function rand(min, max) { return Math.random() * (max - min) + min; }

      function createStars() {
        stars.length = 0;
        for (let i = 0; i < STAR_COUNT; i++) {
          stars.push({
            x: rand(0, width),
            y: rand(0, height),
            r: rand(0.5, 1.8),
            v: rand(10, 40),       // vertical drift speed
            a: rand(0.2, 0.9)      // base alpha
          });
        }
      }

      let last = 0;
      function tick(ts) {
        if (!last) last = ts;
        const dt = (ts - last) / 1000;
        last = ts;

        ctx.clearRect(0, 0, width, height);
        for (const s of stars) {
          s.y += s.v * dt;
          if (s.y > height) { s.y = -2; s.x = rand(0, width); }

          ctx.globalAlpha = s.a + 0.2 * Math.sin(ts / 500 + s.x);
          ctx.fillStyle = '#ffffff';
          ctx.beginPath();
          ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
          ctx.fill();
        }
        requestAnimationFrame(tick);
      }

      function start() {
        resize();
        createStars();
        requestAnimationFrame(tick);
      }

      w.addEventListener('resize', () => { resize(); createStars(); });
      start();
    }
  };
  w.SpaceBG = SpaceBG;
})(window);