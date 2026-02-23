/**
 * Cloudflare Pages Function
 *
 * GET /api/proxy?url=https%3A%2F%2Fexample.com%2Ffile.pdf
 *
 * Used to bypass browser CORS limitations by fetching server-side.
 * Includes basic SSRF protections (blocks localhost/private IP ranges).
 */
export const onRequestGet: PagesFunction = async ({ request }) => {
  const reqUrl = new URL(request.url);
  const target = reqUrl.searchParams.get("url");
  if (!target) return new Response("Missing 'url' query parameter", { status: 400 });

  let u: URL;
  try {
    u = new URL(target);
  } catch {
    return new Response("Invalid URL", { status: 400 });
  }

  if (u.protocol !== "https:" && u.protocol !== "http:") {
    return new Response("Only http(s) URLs are allowed", { status: 400 });
  }

  const host = u.hostname;
  if (isBlockedHost(host)) {
    return new Response("Blocked host", { status: 403 });
  }

  let upstream: Response;
  try {
    upstream = await fetch(u.toString(), {
      redirect: "follow",
      headers: {
        // Some hosts reject requests without a UA.
        "user-agent": "RapidReader/1.0",
      },
    });
  } catch (e: any) {
    return new Response(e?.message ?? "Upstream fetch failed", { status: 502 });
  }

  if (!upstream.ok) {
    return new Response(`Upstream HTTP ${upstream.status} ${upstream.statusText}`, {
      status: upstream.status,
      statusText: upstream.statusText,
    });
  }

  const headers = new Headers();
  headers.set("cache-control", "no-store");
  headers.set("access-control-allow-origin", "*");
  headers.set("x-content-type-options", "nosniff");

  const contentType = upstream.headers.get("content-type");
  if (contentType) headers.set("content-type", contentType);

  const contentDisposition = upstream.headers.get("content-disposition");
  if (contentDisposition) headers.set("content-disposition", contentDisposition);

  return new Response(upstream.body, { status: 200, headers });
};

function isBlockedHost(hostname: string): boolean {
  const h = hostname.toLowerCase();
  if (h === "localhost" || h.endsWith(".localhost") || h.endsWith(".local")) return true;

  // IPv6 localhost / link-local / unique local
  if (h === "::1") return true;
  if (h.startsWith("fe80:")) return true; // link-local
  if (h.startsWith("fc") || h.startsWith("fd")) return true; // unique local (fc00::/7)

  // IPv4 literals (basic check)
  const m = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/.exec(h);
  if (!m) return false;

  const a = Number(m[1]);
  const b = Number(m[2]);
  const c = Number(m[3]);
  const d = Number(m[4]);
  if ([a, b, c, d].some((x) => Number.isNaN(x) || x < 0 || x > 255)) return true;

  // 0.0.0.0/8, 10.0.0.0/8, 127.0.0.0/8
  if (a === 0 || a === 10 || a === 127) return true;
  // 169.254.0.0/16 (link-local)
  if (a === 169 && b === 254) return true;
  // 172.16.0.0/12
  if (a === 172 && b >= 16 && b <= 31) return true;
  // 192.168.0.0/16
  if (a === 192 && b === 168) return true;

  return false;
}

