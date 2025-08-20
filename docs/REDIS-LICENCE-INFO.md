## Hosting your own Redis or Redis Stack on‑premises

Licensing History & What’s Allowed On-Prem

1. Pre-2024 (Redis ≤ 7.2) – BSD-3-Clause (Fully Open Source)

1. Redis versions up to 7.2.x were licensed under a permissive BSD‑3‑Clause license, allowing any use—including commercial, service, and distribution—without restriction. ￼ ￼

1. March 2024 – Switch to Source-Available Dual License (RSALv2 + SSPLv1)
Starting with Redis 7.4, Redis (and Redis Stack modules) moved away from BSD and adopted a dual license: RSALv2 and SSPLv1. ￼

RSALv2 (Redis Source Available License v2)
•	Permissive in allowing use, copying, distribution, modifications and derivative works.
•	Restrictions:
•	You cannot commercialize the software or offer it as a managed service to others.
•	You must preserve all license and copyright notices. ￼

SSPLv1 (Server Side Public License v1)
•	A copyleft-like license based on AGPL, but broader.
•	If you provide the software (modified or not) as a service, you must open-source the entire stack—including management tools, interfaces, automation, monitoring, etc.—under SSPL. ￼ ￼
•	Note: SSPL is not recognized as open source by OSI. ￼

— On‑Prem Use: Both licenses generally permit internal, self‑hosted use of Redis for any purpose, as long as you’re not offering it to third parties as a service or product. ￼

⸻

1. May 2025 – Redis 8: Addition of AGPLv3 License
   •	With Redis 8, Redis introduced a third licensing option: AGPLv3, alongside RSALv2 and SSPLv1. ￼
   •	This makes the licensing model a tri-license: RSALv2, SSPLv1, or AGPLv3.
   •	AGPLv3 is an OSI‑approved open source license, with strong copyleft terms, and requires that if you offer the software over a network, you must make the corresponding source code available. ￼

— On‑Prem Use: With AGPLv3, if you host Redis internally and do not expose it as a networked service to third parties, you’re fine. But if you expose it externally (i.e. to users), AGPLv3 obligates you to release your modifications. RSALv2 and SSPLv1 remain options as well.

What Should You Do for On-Prem Hosting?
•	Internal, private use (e.g., caching, backend data store):
•	Versions ≤7.2: Safe under BSD.
•	Versions 7.4–7.x: Can use under RSALv2—as long as it’s not offered as a service to others.
•	Version 8+: Use AGPLv3 if you may expose it; otherwise, RSALv2 remains fine.
•	Offering Redis as a service or managed product to others:
•	Under RSALv2: Not allowed.
•	Under SSPLv1: Only allowed if you open-source your entire platform stack.
•	Under AGPLv3: Allowed if you release your service’s code publicly.

⸻

Final Notes
•	The licensing shift reflects Redis’s efforts to curb cloud providers from commoditizing their open source work without contributing back. ￼ ￼ ￼ ￼
•	If your use is internal only, you’re generally in the clear—even with newer versions.
•	If in doubt or planning to offer a Redis-based service, consult your legal counsel and evaluate the available licensing options carefully.

Let me know if you’d like help comparing Redis to open-source forks like Valkey, which retains BSD licensing, or exploring enterprise licensing options!


