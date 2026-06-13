/**
 * 专家详情与预约（预留）
 * GET /api/expert/:id
 * GET /api/expert/:id/slots?date=YYYY-MM-DD
 * POST /api/v1/orders
 * POST /api/order/create — 旧路径兜底
 */
import { request } from "@/api/request.js";
import { createOrderV1 } from "@/api/modules/orders.js";
import { getMockCoachById } from "@/api/modules/coach.js";
import { maskUserDisplay } from "@/utils/common/maskUser";

function hashCode(str) {
  let h = 0;
  for (let i = 0; i < str.length; i += 1) {
    h = (h << 5) - h + str.charCodeAt(i);
    h |= 0;
  }
  return Math.abs(h);
}

/**
 * @param {string} id
 * @returns {Promise<import('@/types/booking/expert').ExpertDetail>}
 */
export async function fetchExpert(id) {
  try {
    const data = await request({ url: `/expert/${encodeURIComponent(id)}`, method: "GET" });
    if (data && typeof data === "object" && data.id) {
      return /** @type {import('@/types/booking/expert').ExpertDetail} */ (data);
    }
  } catch {
    /* 无后端时使用 mock */
  }
  return buildMockExpertDetail(id);
}

/**
 * @param {string} id
 * @param {string} date YYYY-MM-DD
 * @returns {Promise<import('@/types/booking/expert').ExpertTimeSlot[]>}
 */
export async function fetchExpertSlots(id, date) {
  try {
    const data = await request({
      url: `/expert/${encodeURIComponent(id)}/slots?date=${encodeURIComponent(date)}`,
      method: "GET",
    });
    if (Array.isArray(data)) {
      return /** @type {import('@/types/booking/expert').ExpertTimeSlot[]} */ (data);
    }
    if (data && Array.isArray(data.slots)) {
      return data.slots;
    }
  } catch {
    /* mock */
  }
  return buildMockSlots(id, date);
}

/**
 * @param {import('@/types/booking/expert').CreateOrderPayload} payload
 * @returns {Promise<{ orderId: string }>}
 */
export async function createOrder(payload) {
  const coachNum = parseInt(String(payload.expertId || "").replace(/\D/g, ""), 10) || 1;
  try {
    return await createOrderV1({
      productId: payload.productId ?? 1,
      coachId: coachNum,
      sceneId: payload.sceneId ?? 1,
      amount: payload.amount ?? 99,
      slotId: payload.slotId,
      date: payload.dateIso,
    });
  } catch {
    /* mock */
  }
  try {
    const data = await request({
      url: "/order/create",
      method: "POST",
      data: payload,
    });
    if (data && data.orderId) {
      return data;
    }
  } catch {
    /* mock */
  }
  await new Promise((r) => setTimeout(r, 400));
  return { orderId: `mock-${Date.now()}` };
}

/**
 * @param {string} id
 * @returns {import('@/types/booking/expert').ExpertDetail}
 */
function buildMockExpertDetail(id) {
  const coach = getMockCoachById(id);
  const name = coach?.name ?? "语境导师";
  const jobTitle = coach?.jobTitle ?? "资深沟通陪练";
  const price = coach?.price ?? 299;
  const sessionMinutes = coach?.sessionMinutes ?? 45;
  const rating = coach?.rating ?? 4.9;
  const orderCount = coach?.orderCount ?? 320;
  const intro =
    coach?.bio ??
    "十余年一线大厂与咨询背景，长期专注高压场景下的表达与逻辑训练。课程以真实案例为骨架，配合逐句复盘与可复用的表达模板，帮助你在有限时间内建立稳定输出习惯。\n\n陪练过程强调「听得懂追问、接得住打断、收得拢结论」，适合希望在面试、汇报、关键对话中快速进阶的学员。";

  const domains = [
    {
      id: "d1",
      title: coach?.skillTags?.[0] ?? "逻辑拆解",
      summary: "从 STAR 到技术深挖，帮你把项目经历讲清、讲透、经得起追问。",
    },
    {
      id: "d2",
      title: coach?.skillTags?.[1] ?? "压力对话",
      summary: "模拟打断与质疑节奏，训练临场补位与情绪稳定。",
    },
    {
      id: "d3",
      title: coach?.skillTags?.[2] ?? "复盘输出",
      summary: "课后结构化反馈：亮点、风险点、下一轮可执行清单。",
    },
    {
      id: "d4",
      title: "表达结构",
      summary: "结论先行、论据分层、收口话术，一套可迁移的叙述框架。",
    },
  ];

  const rawNames = ["wang_lei_9", "张晨", "li_ming_2", "赵一", "sarah_k", "周洋"];
  const reviews = [0, 1, 2, 3].map((i) => {
    const rn = rawNames[i % rawNames.length];
    return {
      id: `rev-${id}-${i}`,
      displayName: maskUserDisplay(rn),
      rating: 5 - (i % 3) * 0.5,
      date: `2026-0${(i % 6) + 1}-${String(10 + i * 3).padStart(2, "0")}`,
      content:
        i % 2 === 0
          ? "节奏很好，追问很贴近真实面试，复盘时指出了我叙述里两处逻辑跳跃，收获很大。"
          : "老师很耐心，会主动降速让我整理思路，结束时有清晰的下次练习建议。",
      avatarLetter: (rn[0] || "学").toUpperCase(),
    };
  });

  return {
    id,
    name,
    jobTitle,
    rating,
    orderCount,
    price,
    sessionMinutes,
    intro,
    domains,
    reviews,
  };
}

/**
 * @param {string} id
 * @param {string} date
 * @returns {import('@/types/booking/expert').ExpertTimeSlot[]}
 */
function buildMockSlots(id, date) {
  const seed = hashCode(`${id}|${date}`);
  const base = ["09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "19:00", "20:00"];
  return base.map((start, i) => {
    const endH = Number(start.split(":")[0]) + 1;
    const end = `${String(endH).padStart(2, "0")}:00`;
    const booked = (seed + i) % 5 === 0;
    return {
      id: `slot-${date}-${i}`,
      label: `${start}-${end}`,
      booked,
    };
  });
}
