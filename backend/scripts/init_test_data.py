#!/usr/bin/env python3
"""
社团管理系统测试数据初始化脚本
功能：第一个用户为系统管理员，然后创建3个社团、3个负责人、3个老师、3个学生，每社团发布1个活动
"""
import requests
import time
import json

BASE_URL = "http://localhost:8080/api"
HEADERS = {"Content-Type": "application/json"}


def register_admin():
    """注册第一个用户为系统管理员"""
    user_data = {
        "username": "admin",
        "password": "admin123",
        "realName": "系统管理员",
        "email": "admin@community.com",
        "phone": "13800000000",
        "department": "校务处",
        "major": "管理",
        "grade": "2026",
        "studentId": "A2026000"
    }
    res = requests.post(
        f"{BASE_URL}/auth/register",
        json=user_data,
        headers=HEADERS
    )
    if res.status_code == 200:
        data = res.json()
        token = data["data"]["token"]
        print(f"✓ Admin registered and token obtained")
        return token
    raise Exception(f"Admin registration failed: {res.text}")


def create_user(token, username, password, real_name, role, student_id=None):
    """创建用户 (管理员权限)"""
    user_data = {
        "username": username,
        "realName": real_name,
        "email": f"{username}@community.com",
        "phone": f"138{username[:8].zfill(8)}",
        "department": "计算机学院",
        "major": "软件工程",
        "grade": "2026",
        "role": role,
        "studentId": student_id or f"S{username[:8].upper().zfill(8)}"
    }
    res = requests.post(
        f"{BASE_URL}/users/admin/create?password={password}",
        json=user_data,
        headers={**HEADERS, "Authorization": f"Bearer {token}"}
    )
    if res.status_code == 200:
        data = res.json()
        print(f"✓ Created user: {username} ({real_name})")
        return data["data"]["id"]
    else:
        print(f"✗ Failed to create user {username}: {res.text}")
        return None


def login_user(username, password):
    """用户登录"""
    res = requests.post(
        f"{BASE_URL}/auth/login",
        json={"username": username, "password": password},
        headers=HEADERS
    )
    if res.status_code == 200:
        data = res.json()
        return data["data"]["token"]
    return None


def create_club_by_admin(admin_token, club_name, club_type, purpose, description, leader_id):
    """管理员创建社团 (直接通过审核)"""
    club_data = {
        "name": club_name,
        "type": club_type,
        "purpose": purpose,
        "description": description,
        "contactEmail": f"{club_name.lower().replace(' ', '')}@community.com",
        "location": "学生活动中心"
    }
    res = requests.post(
        f"{BASE_URL}/clubs/admin/create?leaderId={leader_id}",
        json=club_data,
        headers={**HEADERS, "Authorization": f"Bearer {admin_token}"}
    )
    if res.status_code == 200:
        data = res.json()
        print(f"✓ Created club: {club_name}")
        return data["data"]["id"]
    else:
        print(f"✗ Failed to create club {club_name}: {res.text}")
        return None


def add_club_member(leader_token, club_id, user_token, username, role="MEMBER"):
    """添加社团成员（通过加入申请）"""
    # 先以用户身份申请加入
    res = requests.post(
        f"{BASE_URL}/clubs/{club_id}/join",
        json={"applicationReason": f"申请加入 - 自动生成测试数据"},
        headers={**HEADERS, "Authorization": f"Bearer {user_token}"}
    )
    
    if res.status_code != 200:
        print(f"✗ Failed to apply club {club_id} for {username}: {res.text}")
        return False
    
    application_data = res.json()
    member_id = application_data["data"]["id"]
    
    # 负责人审批通过
    res2 = requests.put(
        f"{BASE_URL}/clubs/members/{member_id}/review",
        json={"approved": True},
        headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
    )
    
    if res2.status_code != 200:
        print(f"✗ Failed to approve member {member_id}: {res2.text}")
        return False
    
    # 如果需要设置角色(如TEACHER)
    if role != "MEMBER":
        res3 = requests.put(
            f"{BASE_URL}/clubs/members/{member_id}/role",
            json={"role": role, "position": "指导老师" if role == "TEACHER" else "成员"},
            headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
        )
        if res3.status_code == 200:
            print(f"✓ Added member {username} to club {club_id} as {role}")
        else:
            print(f"✗ Failed to set role for {username}: {res3.text}")
    else:
        print(f"✓ Added member {username} to club {club_id}")
    
    return True


def create_activity(leader_token, club_id, title, description, location):
    """创建活动 (提交审批)"""
    from datetime import datetime, timedelta
    
    now = datetime.now()
    activity_data = {
        "title": title,
        "description": description,
        "clubId": club_id,
        "startTime": (now + timedelta(days=7)).isoformat(),
        "endTime": (now + timedelta(days=7, hours=3)).isoformat(),
        "registrationDeadline": (now + timedelta(days=5)).isoformat(),
        "location": location,
        "maxParticipants": 50,
        "checkInType": "QR_CODE"
    }
    
    res = requests.post(
        f"{BASE_URL}/activities",
        json=activity_data,
        headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
    )
    
    if res.status_code == 200:
        data = res.json()
        activity_id = data["data"]["id"]
        print(f"✓ Created activity: {title} (id={activity_id})")
        return activity_id
    else:
        print(f"✗ Failed to create activity {title}: {res.text}")
        return None


def approve_activity(admin_token, activity_id):
    """管理员审批活动通过"""
    res = requests.put(
        f"{BASE_URL}/activities/admin/{activity_id}/approve",
        json={"approved": True},
        headers={**HEADERS, "Authorization": f"Bearer {admin_token}"}
    )
    if res.status_code == 200:
        print(f"✓ Activity {activity_id} approved!")
        return True
    else:
        print(f"✗ Failed to approve activity {activity_id}: {res.text}")
        return False


def publish_activity(leader_token, activity_id):
    """发布活动"""
    res = requests.put(
        f"{BASE_URL}/activities/{activity_id}/publish",
        headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
    )
    if res.status_code == 200:
        print(f"✓ Activity {activity_id} published!")
        return True
    else:
        print(f"✗ Failed to publish activity {activity_id}: {res.text}")
        return False


def create_started_activity(leader_token, club_id, title, description, location):
    """创建一个已经开始的活动（startTime在过去，endTime在未来）"""
    from datetime import datetime, timedelta
    
    now = datetime.now()
    activity_data = {
        "title": title,
        "description": description,
        "clubId": club_id,
        "startTime": (now - timedelta(hours=1)).isoformat(),
        "endTime": (now + timedelta(hours=5)).isoformat(),
        "registrationDeadline": (now - timedelta(hours=2)).isoformat(),
        "location": location,
        "maxParticipants": 30,
        "checkInType": "MANUAL"
    }
    
    res = requests.post(
        f"{BASE_URL}/activities",
        json=activity_data,
        headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
    )
    
    if res.status_code == 200:
        data = res.json()
        activity_id = data["data"]["id"]
        print(f"✓ Created started activity: {title} (id={activity_id})")
        return activity_id
    else:
        print(f"✗ Failed to create started activity {title}: {res.text}")
        return None


def register_activity(user_token, activity_id):
    """用户报名活动"""
    res = requests.post(
        f"{BASE_URL}/activities/{activity_id}/register",
        json={},
        headers={**HEADERS, "Authorization": f"Bearer {user_token}"}
    )
    if res.status_code == 200:
        print(f"✓ Registered for activity {activity_id}")
        return True
    else:
        print(f"✗ Failed to register for activity {activity_id}: {res.text}")
        return False


def manual_check_in(leader_token, activity_id, user_id):
    """社团负责人为用户手动签到"""
    res = requests.post(
        f"{BASE_URL}/activities/{activity_id}/manual-check-in/{user_id}",
        headers={**HEADERS, "Authorization": f"Bearer {leader_token}"}
    )
    if res.status_code == 200:
        print(f"✓ Manual check-in for user {user_id} in activity {activity_id}")
        return True
    else:
        print(f"✗ Failed manual check-in for user {user_id}: {res.text}")
        return False


def main():
    print("=" * 60)
    print("社团管理系统测试数据初始化")
    print("=" * 60)
    
    # 1. 注册第一个用户为系统管理员
    print("\n[1] 注册系统管理员...")
    admin_token = register_admin()
    
    # 2. 创建用户
    print("\n[2] 创建测试用户...")
    
    # 3个社团负责人
    leader1_id = create_user(admin_token, "leader1", "leader123", "社长李明", "CLUB_LEADER", "L2026001")
    leader2_id = create_user(admin_token, "leader2", "leader123", "社长王华", "CLUB_LEADER", "L2026002")
    leader3_id = create_user(admin_token, "leader3", "leader123", "社长张伟", "CLUB_LEADER", "L2026003")
    
    # 3个指导老师
    teacher1_id = create_user(admin_token, "teacher1", "teacher123", "指导老师陈老师", "TEACHER", "T2026001")
    teacher2_id = create_user(admin_token, "teacher2", "teacher123", "指导老师刘老师", "TEACHER", "T2026002")
    teacher3_id = create_user(admin_token, "teacher3", "teacher123", "指导老师赵老师", "TEACHER", "T2026003")
    
    # 3个学生
    student1_id = create_user(admin_token, "student1", "student123", "学生小红", "USER", "S2026001")
    student2_id = create_user(admin_token, "student2", "student123", "学生小刚", "USER", "S2026002")
    student3_id = create_user(admin_token, "student3", "student123", "学生小美", "USER", "S2026003")
    
    time.sleep(1)
    
    # 3. 创建3个社团
    print("\n[3] 创建3个社团...")
    club1_id = create_club_by_admin(
        admin_token, "人工智能研究社", "ACADEMIC",
        "探索AI前沿技术，培养创新思维",
        "人工智能研究社致力于AI技术研究与应用，组织算法竞赛、技术分享等活动。",
        leader1_id
    )
    
    club2_id = create_club_by_admin(
        admin_token, "羽毛球俱乐部", "SPORTS",
        "强身健体，增进友谊",
        "羽毛球俱乐部是校内最活跃的体育社团之一，定期组织训练和比赛。",
        leader2_id
    )
    
    club3_id = create_club_by_admin(
        admin_token, "爱心志愿者协会", "VOLUNTEER",
        "奉献爱心，服务社会",
        "爱心志愿者协会组织各类公益活动，传递正能量，服务社区。",
        leader3_id
    )
    
    time.sleep(1)
    
    # 4. 为每个社团添加指导老师和学生
    print("\n[4] 添加社团成员...")
    
    # 获取各用户token
    leader1_token = login_user("leader1", "leader123")
    leader2_token = login_user("leader2", "leader123")
    leader3_token = login_user("leader3", "leader123")
    
    teacher1_token = login_user("teacher1", "teacher123")
    teacher2_token = login_user("teacher2", "teacher123")
    teacher3_token = login_user("teacher3", "teacher123")
    
    student1_token = login_user("student1", "student123")
    student2_token = login_user("student2", "student123")
    student3_token = login_user("student3", "student123")
    
    # 社团1: teacher1 + student1
    add_club_member(leader1_token, club1_id, teacher1_token, "teacher1", "TEACHER")
    add_club_member(leader1_token, club1_id, student1_token, "student1", "MEMBER")
    
    # 社团2: teacher2 + student2
    add_club_member(leader2_token, club2_id, teacher2_token, "teacher2", "TEACHER")
    add_club_member(leader2_token, club2_id, student2_token, "student2", "MEMBER")
    
    # 社团3: teacher3 + student3
    add_club_member(leader3_token, club3_id, teacher3_token, "teacher3", "TEACHER")
    add_club_member(leader3_token, club3_id, student3_token, "student3", "MEMBER")
    
    time.sleep(1)
    
    # 5. 每个社团创建1个活动
    print("\n[5] 创建活动并审批...")
    
    activity1_id = create_activity(
        leader1_token, club1_id,
        "机器学习实战工作坊",
        "从零开始学习机器学习，实战项目讲解，欢迎所有对AI感兴趣的同学参加！",
        "计算机楼301实验室"
    )
    
    activity2_id = create_activity(
        leader2_token, club2_id,
        "羽毛球友谊赛",
        "举办校内羽毛球友谊赛，欢迎所有羽毛球爱好者参加，增进友谊，强身健体！",
        "体育馆羽毛球场"
    )
    
    activity3_id = create_activity(
        leader3_token, club3_id,
        "社区关爱行动",
        "组织志愿者前往社区，为老人提供陪伴和帮助，传递温暖与关怀。",
        "社区服务中心"
    )
    
    time.sleep(1)
    
    # 6. 管理员审批活动
    print("\n[6] 审批活动...")
    if activity1_id:
        approve_activity(admin_token, activity1_id)
        publish_activity(leader1_token, activity1_id)
    
    if activity2_id:
        approve_activity(admin_token, activity2_id)
        publish_activity(leader2_token, activity2_id)
    
    if activity3_id:
        approve_activity(admin_token, activity3_id)
        publish_activity(leader3_token, activity3_id)
    
    time.sleep(1)
    
    # 7. 创建一个已经开始的活动（用于测试签到和参与人数显示）
    print("\n[7] 创建已开始的活动...")
    
    started_activity_id = create_started_activity(
        leader1_token, club1_id,
        "AI编程马拉松",
        "24小时AI编程挑战赛，已经开始！快来签到参加吧！",
        "图书馆多功能厅"
    )
    
    if started_activity_id:
        approve_activity(admin_token, started_activity_id)
        publish_activity(leader1_token, started_activity_id)
        
        time.sleep(1)
        
        # 学生报名
        print("\n[8] 学生报名已开始的活动...")
        register_activity(student1_token, started_activity_id)
        register_activity(student2_token, started_activity_id)
        register_activity(student3_token, started_activity_id)
        
        time.sleep(1)
        
        # 手动签到部分学生
        print("\n[9] 签到部分学生...")
        if student1_id:
            manual_check_in(leader1_token, started_activity_id, student1_id)
        if student2_id:
            manual_check_in(leader1_token, started_activity_id, student2_id)
    
    print("\n" + "=" * 60)
    print("✓ 测试数据初始化完成!")
    print("=" * 60)
    print("\n创建的账号:")
    print("  系统管理员: admin/admin123")
    print("  社团负责人: leader1/leader123, leader2/leader123, leader3/leader123")
    print("  指导老师:   teacher1/teacher123, teacher2/teacher123, teacher3/teacher123")
    print("  学生:       student1/student123, student2/student123, student3/student123")
    print("\n创建的社团:")
    print(f"  1. 人工智能研究社 (ID: {club1_id})")
    print(f"  2. 羽毛球俱乐部 (ID: {club2_id})")
    print(f"  3. 爱心志愿者协会 (ID: {club3_id})")
    print("\n创建的活动:")
    print(f"  1. 机器学习实战工作坊 (ID: {activity1_id}) - 未开始")
    print(f"  2. 羽毛球友谊赛 (ID: {activity2_id}) - 未开始")
    print(f"  3. 社区关爱行动 (ID: {activity3_id}) - 未开始")
    print(f"  4. AI编程马拉松 (ID: {started_activity_id}) - 已开始，3人报名，2人签到")
    print("=" * 60)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
